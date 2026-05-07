package com.rafa.musicas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class MusicEntity(
    @PrimaryKey
    val uri: String,
    val fileName: String,
    val displayName: String,
    val author: String,
    val durationMs: Long?,
    val album: String? = null,
    val artworkUri: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0,
    val isFavorite: Boolean = false
)
