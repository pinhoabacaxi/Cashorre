package com.rafa.musicas.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: MusicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<MusicEntity>)

    @Query("SELECT * FROM tracks ORDER BY displayName COLLATE NOCASE ASC")
    fun observeAllTracks(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY displayName COLLATE NOCASE ASC")
    fun observeFavorites(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun observeRecentlyPlayed(limit: Int = 100): Flow<List<MusicEntity>>

    @Query("UPDATE tracks SET isFavorite = :favorite WHERE uri = :uri")
    suspend fun setFavorite(uri: String, favorite: Boolean)

    @Query(
        """
        UPDATE tracks 
        SET lastPlayedAt = :timestamp, playCount = playCount + 1 
        WHERE uri = :uri
        """
    )
    suspend fun markPlayed(uri: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM tracks WHERE uri = :uri")
    suspend fun deleteTrack(uri: String)
}
