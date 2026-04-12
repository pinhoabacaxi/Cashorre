package com.rafa.musicas.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Playlists are stored in: filesDir/midias/<playlistName>/
 * Tracks are copied into that folder.
 * Track order + display names stored in: filesDir/midias/<playlistName>/order.json
 */
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

    fun renamePlaylist(oldName: String, newName: String): String {
        val oldFolder = File(mediaRoot(), oldName)
        val safe = sanitizeName(newName)
        val newFolder = File(mediaRoot(), safe)
        if (oldFolder.exists() && oldFolder.isDirectory) {
            oldFolder.renameTo(newFolder)
        } else {
            newFolder.mkdirs()
        }
        // Ensure order file exists
        ensureOrderFile(safe)
        val idx = readIndex(safe)
        writeIndex(safe, idx.copy(playlistName = safe))
        return safe
    }

    fun deletePlaylist(name: String) {
        File(mediaRoot(), name).deleteRecursively()
    }

    fun playlistFolder(name: String): File = File(mediaRoot(), name)

    private fun orderFile(playlistName: String): File =
        File(playlistFolder(playlistName), "order.json")

    private fun ensureOrderFile(playlistName: String) {
        val folder = playlistFolder(playlistName)
        if (!folder.exists()) folder.mkdirs()
        val f = orderFile(playlistName)
        if (!f.exists()) {
            f.writeText(json.encodeToString(PlaylistIndex(playlistName, emptyList())))
        }
    }

    fun readIndex(playlistName: String): PlaylistIndex {
        ensureOrderFile(playlistName)
        val f = orderFile(playlistName)
        return runCatching { json.decodeFromString<PlaylistIndex>(f.readText()) }
            .getOrElse { PlaylistIndex(playlistName, emptyList()) }
    }

    fun writeIndex(playlistName: String, index: PlaylistIndex) {
        ensureOrderFile(playlistName)
        orderFile(playlistName).writeText(json.encodeToString(index))
    }

    data class TrackItem(val entry: TrackEntry, val file: File)

    fun listTrackItems(playlistName: String): List<TrackItem> {
        val folder = playlistFolder(playlistName)
        ensureOrderFile(playlistName)
        val idx = readIndex(playlistName)

        val filesByName = folder.listFiles()
            ?.filter { it.isFile && isAudio(it.name) }
            ?.associateBy { it.name }
            ?: emptyMap()

        val ordered = idx.tracks.mapNotNull { e ->
            val f = filesByName[e.fileName] ?: return@mapNotNull null
            TrackItem(e, f)
        }

        val remainingFiles = filesByName.values
            .filter { f -> idx.tracks.none { it.fileName == f.name } }
            .sortedBy { it.name }

        // Append remaining files to index (default at end)
        if (remainingFiles.isNotEmpty()) {
            val updated = idx.tracks.toMutableList()
            remainingFiles.forEach { f ->
                updated.add(TrackEntry(fileName = f.name, displayName = f.name))
            }
            writeIndex(playlistName, idx.copy(tracks = updated))
        }

        val updatedIdx = readIndex(playlistName)
        val updatedOrdered = updatedIdx.tracks.mapNotNull { e ->
            val f = filesByName[e.fileName] ?: return@mapNotNull null
            TrackItem(e, f)
        }
        return updatedOrdered
    }

    fun reorder(playlistName: String, from: Int, to: Int) {
        val idx = readIndex(playlistName)
        val tracks = idx.tracks.toMutableList()
        if (from !in tracks.indices || to !in tracks.indices) return
        val item = tracks.removeAt(from)
        tracks.add(to, item)
        writeIndex(playlistName, idx.copy(tracks = tracks))
    }

    fun updateDisplayName(playlistName: String, fileName: String, newDisplayName: String) {
        val idx = readIndex(playlistName)
        val updated = idx.tracks.map { t ->
            if (t.fileName == fileName) t.copy(displayName = newDisplayName.trim().ifBlank { t.displayName })
            else t
        }
        writeIndex(playlistName, idx.copy(tracks = updated))
    }

    fun removeTrack(playlistName: String, fileName: String, deleteFile: Boolean = true) {
        val folder = playlistFolder(playlistName)
        if (deleteFile) {
            File(folder, fileName).delete()
        }
        val idx = readIndex(playlistName)
        val updated = idx.tracks.filterNot { it.fileName == fileName }
        writeIndex(playlistName, idx.copy(tracks = updated))
    }

    fun addTracksFromTree(treeUri: Uri, playlistName: String, recursive: Boolean = true): Int {
        val cr = context.contentResolver
        val tree = DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        val destFolder = playlistFolder(playlistName)
        if (!destFolder.exists()) destFolder.mkdirs()
        ensureOrderFile(playlistName)

        val audioDocs = mutableListOf<DocumentFile>()
        fun walk(dir: DocumentFile) {
            dir.listFiles().forEach { f ->
                if (f.isDirectory && recursive) walk(f)
                else if (f.isFile && isAudio(f.name ?: "")) audioDocs.add(f)
            }
        }
        walk(tree)

        var copied = 0
        audioDocs.forEach { doc ->
            val name = doc.name ?: return@forEach
            val dest = uniqueFile(destFolder, name)
            cr.openInputStream(doc.uri)?.use { input ->
                dest.outputStream().use { out -> input.copyTo(out) }
                copied++
            }
        }

        if (copied > 0) {
            // Append new files at end
            val idx = readIndex(playlistName)
            val idxNames = idx.tracks.map { it.fileName }.toMutableSet()
            val allFiles = destFolder.listFiles()
                ?.filter { it.isFile && isAudio(it.name) }
                ?.map { it.name }
                ?: emptyList()
            val newOnDisk = allFiles.filter { it !in idxNames }.sorted()
            if (newOnDisk.isNotEmpty()) {
                val updated = idx.tracks.toMutableList()
                newOnDisk.forEach { fn -> updated.add(TrackEntry(fileName = fn, displayName = fn)) }
                writeIndex(playlistName, idx.copy(tracks = updated))
            }
        }

        return copied
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
        val trimmed = name.trim().ifEmpty { "Playlist" }
        val cleaned = trimmed.replace(Regex("[\\/:*?\"<>|]"), "_")
        return cleaned.take(60)
    }
}
