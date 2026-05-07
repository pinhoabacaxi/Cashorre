package com.rafa.musicas.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object MusicScanner {

    private val supportedMimeTypes = setOf(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/flac",
        "audio/ogg",
        "audio/wav",
        "audio/x-wav",
        "audio/opus"
    )

    fun scanDeviceAudio(context: Context): List<ScannedTrack> {
        val tracks = mutableListOf<ScannedTrack>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val mime = cursor.getString(mimeCol)
                if (mime != null && mime !in supportedMimeTypes) continue

                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)

                val audioUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val artworkUri = if (albumId > 0L) {
                    ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()
                } else {
                    null
                }

                tracks += ScannedTrack(
                    uri = audioUri.toString(),
                    fileName = cursor.getString(nameCol) ?: "audio_$id",
                    displayName = cursor.getString(titleCol)
                        ?: cursor.getString(nameCol)
                        ?: "Música sem nome",
                    author = cursor.getString(artistCol) ?: "Desconhecido",
                    album = cursor.getString(albumCol),
                    artworkUri = artworkUri,
                    durationMs = cursor.getLong(durationCol)
                )
            }
        }

        return tracks
    }
}

data class ScannedTrack(
    val uri: String,
    val fileName: String,
    val displayName: String,
    val author: String,
    val album: String?,
    val artworkUri: String?,
    val durationMs: Long
)
