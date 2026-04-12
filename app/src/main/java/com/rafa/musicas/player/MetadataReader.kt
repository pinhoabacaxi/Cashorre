package com.rafa.musicas.player

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class TrackMeta(
    val title: String?,
    val artist: String?,
    val durationMs: Long?,
    val coverPath: String?
)

object MetadataReader {

    private fun coverCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "covers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sha1(s: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val b = md.digest(s.toByteArray(Charsets.UTF_8))
        return b.joinToString("") { "%02x".format(it) }
    }

    fun read(context: Context, file: File): TrackMeta {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(file.absolutePath)
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val art = mmr.embeddedPicture
            val coverPath = if (art != null && art.isNotEmpty()) {
                val hash = sha1(file.absolutePath + file.lastModified().toString())
                val out = File(coverCacheDir(context), "$hash.jpg")
                if (!out.exists()) {
                    FileOutputStream(out).use { it.write(art) }
                }
                out.absolutePath
            } else null
            TrackMeta(title, artist, dur, coverPath)
        } catch (_: Throwable) {
            TrackMeta(null, null, null, null)
        } finally {
            runCatching { mmr.release() }
        }
    }
}
