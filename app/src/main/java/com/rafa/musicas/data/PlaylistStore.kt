package com.rafa.musicas.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class TrackEntry(
    val fileName: String,
    val displayName: String,
    val author: String = "Desconhecido",
    val uri: String? = null,
    val durationMs: Long? = null
)

@Serializable
data class PlaylistIndex(
    val tracks: List<TrackEntry> = emptyList()
)

data class TrackItem(
    val fileName: String,
    val displayName: String,
    val author: String,
    val playlistName: String,
    val uri: String?,
    val durationMs: Long?
) {
    fun toMediaItem(): MediaItem {
        val mediaUri = uri?.let(Uri::parse)

        val metadata = MediaMetadata.Builder()
            .setDisplayTitle(displayName)
            .setArtist(author)
            .build()

        return MediaItem.Builder()
            .setMediaId(uri ?: fileName)
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()
    }
}

class PlaylistStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private fun mediaRoot(): File {
        val root = File(context.filesDir, "midias")
        if (!root.exists()) root.mkdirs()
        return root
    }

    fun listPlaylists(): List<String> =
        mediaRoot().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

    fun createPlaylist(name: String): String {
        val safe = sanitizeName(name)
        val folder = File(mediaRoot(), safe)
        folder.mkdirs()
        ensureOrderFile(safe)
        return safe
    }

    fun listTrackItems(playlistName: String): List<TrackItem> {
        val idx = readIndex(playlistName)
        return idx.tracks.map {
            val localFile = File(File(mediaRoot(), playlistName), it.fileName)
            val fallbackUri = if (localFile.exists()) Uri.fromFile(localFile).toString() else null

            TrackItem(
                fileName = it.fileName,
                displayName = it.displayName,
                author = it.author,
                playlistName = playlistName,
                uri = it.uri ?: fallbackUri,
                durationMs = it.durationMs
            )
        }
    }

    fun renamePlaylist(oldName: String, newName: String): Boolean {
        val oldFolder = File(mediaRoot(), oldName)
        val safe = sanitizeName(newName)
        val newFolder = File(mediaRoot(), safe)

        return if (oldFolder.exists() && oldFolder.isDirectory && !newFolder.exists()) {
            oldFolder.renameTo(newFolder)
        } else {
            false
        }
    }

    fun deletePlaylist(name: String) {
        File(mediaRoot(), name).deleteRecursively()
    }

    fun updateDisplayName(playlistName: String, fileName: String, newDisplayName: String) {
        val idx = readIndex(playlistName)
        val updatedTracks = idx.tracks.map {
            if (it.fileName == fileName) it.copy(displayName = newDisplayName) else it
        }
        writeIndex(playlistName, idx.copy(tracks = updatedTracks))
    }

    fun removeTrack(playlistName: String, fileName: String, deleteFile: Boolean) {
        val idx = readIndex(playlistName)
        val updatedTracks = idx.tracks.filter { it.fileName != fileName }
        writeIndex(playlistName, idx.copy(tracks = updatedTracks))

        if (deleteFile) {
            val file = File(File(mediaRoot(), playlistName), fileName)
            if (file.exists()) file.delete()
        }
    }

    fun addTracksFromMediaStore(playlistName: String): Int {
        val safePlaylist = createPlaylist(playlistName)
        val scanned = MusicScanner.scanDeviceAudio(context)
        val currentIdx = readIndex(safePlaylist)
        val existingUris = currentIdx.tracks.mapNotNull { it.uri }.toSet()

        val newTracks = scanned
            .filter { it.uri !in existingUris }
            .map {
                TrackEntry(
                    fileName = it.fileName,
                    displayName = it.displayName,
                    author = it.author,
                    uri = it.uri,
                    durationMs = it.durationMs
                )
            }

        writeIndex(
            safePlaylist,
            currentIdx.copy(tracks = currentIdx.tracks + newTracks)
        )

        return newTracks.size
    }

    fun addTracksFromTree(playlistName: String, treeUri: Uri, recursive: Boolean): Int {
        val safePlaylist = createPlaylist(playlistName)
        val destFolder = File(mediaRoot(), safePlaylist)
        destFolder.mkdirs()

        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        val files = mutableListOf<DocumentFile>()
        gatherFiles(rootDoc, files, recursive)

        val currentIdx = readIndex(safePlaylist)
        val newTracks = currentIdx.tracks.toMutableList()

        var copied = 0

        files.forEach { doc ->
            val name = doc.name ?: return@forEach
            if (!isAudio(name)) return@forEach

            val destFile = uniqueFile(destFolder, name)

            try {
                context.contentResolver.openInputStream(doc.uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                newTracks.add(
                    TrackEntry(
                        fileName = destFile.name,
                        displayName = destFile.nameWithoutExtension,
                        author = "Desconhecido",
                        uri = Uri.fromFile(destFile).toString()
                    )
                )
                copied++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        writeIndex(safePlaylist, currentIdx.copy(tracks = newTracks))
        return copied
    }

    private fun gatherFiles(doc: DocumentFile, list: MutableList<DocumentFile>, recursive: Boolean) {
        doc.listFiles().forEach {
            if (it.isDirectory && recursive) {
                gatherFiles(it, list, true)
            } else if (it.isFile) {
                list.add(it)
            }
        }
    }

    private fun readIndex(playlistName: String): PlaylistIndex {
        val file = File(File(mediaRoot(), playlistName), "order.json")

        return if (file.exists()) {
            try {
                val content = file.readText()
                if (content.isBlank()) PlaylistIndex()
                else json.decodeFromString<PlaylistIndex>(content)
            } catch (_: Exception) {
                PlaylistIndex()
            }
        } else {
            PlaylistIndex()
        }
    }

    private fun writeIndex(playlistName: String, index: PlaylistIndex) {
        val folder = File(mediaRoot(), playlistName)
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "order.json")
        file.writeText(json.encodeToString(index))
    }

    private fun ensureOrderFile(playlistName: String) {
        val file = File(File(mediaRoot(), playlistName), "order.json")
        if (!file.exists()) writeIndex(playlistName, PlaylistIndex())
    }

    private fun uniqueFile(folder: File, name: String): File {
        var file = File(folder, name)
        if (!file.exists()) return file

        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""

        var i = 1
        while (file.exists()) {
            file = File(folder, "${base}_$i$ext")
            i++
        }

        return file
    }

    private fun isAudio(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mp3") ||
            lower.endsWith(".m4a") ||
            lower.endsWith(".wav") ||
            lower.endsWith(".flac") ||
            lower.endsWith(".ogg") ||
            lower.endsWith(".opus") ||
            lower.endsWith(".aac")
    }

    private fun sanitizeName(name: String): String =
        name.trim()
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .ifEmpty { "Playlist" }
}
