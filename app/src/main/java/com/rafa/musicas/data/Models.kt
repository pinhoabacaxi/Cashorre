package com.rafa.musicas.data

import kotlinx.serialization.Serializable

@Serializable
data class TrackEntry(
    val fileName: String,
    val displayName: String = fileName,
    val title: String? = null,
    val artist: String? = null,
    val durationMs: Long? = null
)

@Serializable
data class PlaylistIndex(
    val playlistName: String,
    val tracks: List<TrackEntry>
)
