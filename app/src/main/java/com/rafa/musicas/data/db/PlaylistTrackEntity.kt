package com.rafa.musicas.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistName", "trackUri"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["name"],
            childColumns = ["playlistName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MusicEntity::class,
            parentColumns = ["uri"],
            childColumns = ["trackUri"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistName"),
        Index("trackUri")
    ]
)
data class PlaylistTrackEntity(
    val playlistName: String,
    val trackUri: String,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)
