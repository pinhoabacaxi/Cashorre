package com.rafa.musicas.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

object MusicScanner {
    private val supportedMimeTypes = setOf(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/flac",
        "audio/ogg",
        "audio/wav",
        "
