package com.rafa.musicas.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    suspend fun getPlaylistsOnce(): List<PlaylistEntity>

    @Query("DELETE FROM playlists WHERE name = :name")
    suspend fun deletePlaylist(name: String)

    @Query(
        """
        UPDATE playlists 
        SET name = :newName, updatedAt = :updatedAt 
        WHERE name = :oldName
        """
    )
    suspend fun renamePlaylist(
        oldName: String,
        newName: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_tracks ON tracks.uri = playlist_tracks.trackUri
        WHERE playlist_tracks.playlistName = :playlistName
        ORDER BY playlist_tracks.position ASC
        """
    )
    fun observePlaylistTracks(playlistName: String): Flow<List<MusicEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistName = :playlistName")
    suspend fun countTracks(playlistName: String): Int

    @Query("DELETE FROM playlist_tracks WHERE playlistName = :playlistName AND trackUri = :trackUri")
    suspend fun removeTrackFromPlaylist(playlistName: String, trackUri: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistName = :playlistName")
    suspend fun clearPlaylist(playlistName: String)
}
