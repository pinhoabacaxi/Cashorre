package com.rafa.musicas.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem // ADICIONADO
import androidx.media3.common.MediaMetadata // ADICIONADO
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Playlists are stored in: filesDir/midias/<playlistName>/
 * Tracks are copied into that folder.
 * Track order + display names stored in: filesDir/midias/<playlistName>/order.json
 */

@Serializable
data class TrackEntry(
    val fileName: String,
    val displayName: String,
    val author: String = "Desconhecido"
)

// Classe de UI que representa uma música na lista
data class TrackItem(
    val fileName: String,
    val displayName: String,
    val author: String,
    val playlistName: String
) {
    // FUNÇÃO ADICIONADA: Converte o modelo interno para o modelo do Player (Media3)
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(fileName) // Usamos o nome do arquivo como ID único
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(displayName)
                    .setArtist(author)
                    .build()
            )
            .build()
    }
}

@Serializable
data class PlaylistIndex(
    val tracks: List<TrackEntry> = emptyList()
)

class PlaylistStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

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
            TrackItem(
                fileName = it.fileName,
                displayName = it.displayName,
                author = it.author,
                playlistName = playlistName
            )
        }
    }

    fun renamePlaylist(oldName: String, newName: String): Boolean {
        val oldFolder = File(mediaRoot(), oldName)
        val safe = sanitizeName(newName)
        val newFolder = File(mediaRoot(), safe)
        return if (oldFolder.exists() && oldFolder.isDirectory && !newFolder.exists()) {
            oldFolder.renameTo(newFolder)
        } else false
    }

    fun deletePlaylist(name: String) {
        val folder = File(mediaRoot(), name)
        folder.deleteRecursively()
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

    fun importFromFolder(playlistName: String, treeUri: Uri, recursive: Boolean): Int {
        val destFolder = File(mediaRoot(), playlistName)
        destFolder.mkdirs()

        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        val files = mutableListOf<DocumentFile>()
        gatherFiles(rootDoc, files, recursive)

        var copied = 0
        val currentIdx = readIndex(playlistName)
        val newTracks = currentIdx.tracks.toMutableList()

        files.forEach { doc ->
            if (isAudio(doc.name ?: "")) {
                val name = doc.name ?: "track"
                val destFile = uniqueFile(destFolder, name)
                
                context.contentResolver.openInputStream(doc.uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                newTracks.add(TrackEntry(fileName = destFile.name, displayName = destFile.name))
                copied++
            }
        }

        writeIndex(playlistName, currentIdx.copy(tracks = newTracks))
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
                json.decodeFromString<PlaylistIndex>(file.readText())
            } catch (e: Exception) {
                PlaylistIndex()
            }
        } else {
            PlaylistIndex()
        }
    }

    private fun writeIndex(playlistName: String, index: PlaylistIndex) {
        val file = File(File(mediaRoot(), playlistName), "order.json")
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

    private fun isAudio(n: String): Boolean {
        val lower = n.lowercase()
        return lower.endsWith(".mp3") || lower.endsWith(".m4a")
    }

    private fun sanitizeName(name: String): String {
        return name.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifEmpty { "Playlist" }
    }
}
