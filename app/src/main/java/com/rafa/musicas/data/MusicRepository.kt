package com.rafa.musicas.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rafa.musicas.data.db.MusicDatabase
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.db.PlaylistEntity
import com.rafa.musicas.data.db.PlaylistTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {

    private val db = MusicDatabase.get(context)
    private val musicDao = db.musicDao()
    private val playlistDao = db.playlistDao()
    private val appContext = context.applicationContext

    fun observeAllTracks(): Flow<List<MusicEntity>> = musicDao.observeAllTracks()
    fun observeFavorites(): Flow<List<MusicEntity>> = musicDao.observeFavorites()
    fun observeRecentlyPlayed(limit: Int = 100): Flow<List<MusicEntity>> = musicDao.observeRecentlyPlayed(limit)
    fun observePlaylists(): Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()
    fun observePlaylistTracks(playlistName: String): Flow<List<MusicEntity>> =
        playlistDao.observePlaylistTracks(playlistName)

    suspend fun scanDeviceToLibrary(): Int = withContext(Dispatchers.IO) {
        val scanned = MusicScanner.scanDeviceAudio(appContext)

        val entities = scanned.map {
            MusicEntity(
                uri = it.uri,
                fileName = it.fileName,
                displayName = it.displayName,
                author = it.author,
                durationMs = it.durationMs
            )
        }

        musicDao.upsertTracks(entities)
        entities.size
    }

    suspend fun createPlaylist(name: String): String = withContext(Dispatchers.IO) {
        val safe = sanitizeName(name)
        playlistDao.insertPlaylist(PlaylistEntity(name = safe))
        safe
    }

    suspend fun deletePlaylist(name: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(name)
    }

    suspend fun renamePlaylist(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(oldName, sanitizeName(newName))
    }

    suspend fun addTrackToPlaylist(
        playlistName: String,
        track: MusicEntity
    ) = withContext(Dispatchers.IO) {
        val safePlaylist = sanitizeName(playlistName)
        playlistDao.insertPlaylist(PlaylistEntity(name = safePlaylist))
        musicDao.upsertTrack(track)

        val position = playlistDao.countTracks(safePlaylist)

        playlistDao.insertPlaylistTrack(
            PlaylistTrackEntity(
                playlistName = safePlaylist,
                trackUri = track.uri,
                position = position
            )
        )
    }

    suspend fun removeTrackFromPlaylist(
        playlistName: String,
        trackUri: String
    ) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistName, trackUri)
    }

    suspend fun setFavorite(uri: String, favorite: Boolean) = withContext(Dispatchers.IO) {
        musicDao.setFavorite(uri, favorite)
    }

    suspend fun markPlayed(uri: String) = withContext(Dispatchers.IO) {
        musicDao.markPlayed(uri)
    suspend fun moveTrackInPlaylist(
        playlistName: String,
        tracks: List<MusicEntity>,
        fromIndex: Int,
        toIndex: Int
    ) = withContext(Dispatchers.IO) {
        if (fromIndex !in tracks.indices || toIndex !in tracks.indices) return@withContext

        val mutable = tracks.toMutableList()
        val moved = mutable.removeAt(fromIndex)
        mutable.add(toIndex, moved)

        mutable.forEachIndexed { index, track ->
            playlistDao.updateTrackPosition(playlistName, track.uri, index)
        }
    }
        }

        private fun sanitizeName(name: String): String =
            name.trim()
                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .ifEmpty { "Playlist" }
    }

    fun MusicEntity.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(uri)
            .setUri(uri)
            .setMediaMetadata(
                 MediaMetadata.Builder()
                    .setDisplayTitle(displayName)
                    .setArtist(author)
                    .build()
            )
             .build()
}
