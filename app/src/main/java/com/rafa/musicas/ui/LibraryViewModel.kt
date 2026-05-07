package com.rafa.musicas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.musicas.data.MusicRepository
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.db.PlaylistEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.rafa.musicas.data.AlbumGroup
import com.rafa.musicas.data.ArtistGroup
import com.rafa.musicas.data.groupTracksByAlbum
import com.rafa.musicas.data.groupTracksByArtist

enum class LibraryFilter {
    ALL,
    FAVORITES,
    RECENT
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.ALL)

    private val allTracks =
        repository.observeAllTracks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tracks: StateFlow<List<MusicEntity>> =
        combine(allTracks, query, filter) { tracks, q, selectedFilter ->
            val base = when (selectedFilter) {
                LibraryFilter.ALL -> tracks
                LibraryFilter.FAVORITES -> tracks.filter { it.isFavorite }
                LibraryFilter.RECENT -> tracks.filter { it.lastPlayedAt != null }
                    .sortedByDescending { it.lastPlayedAt }
            
            }

            val normalizedQuery = q.trim().lowercase()

            if (normalizedQuery.isBlank()) {
                base
            } else {
                base.filter {
                    it.displayName.lowercase().contains(normalizedQuery) ||
                        it.author.lowercase().contains(normalizedQuery) ||
                        (it.album ?: "").lowercase().contains(normalizedQuery)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<ArtistGroup>> =
        combine(allTracks, query) { tracks, q ->
            val filtered = if (q.isBlank()) {
                tracks
            } else {
                tracks.filter {
                    it.author.lowercase().contains(q.lowercase())
                }
            }

            groupTracksByArtist(filtered)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val albums: StateFlow<List<AlbumGroup>> =
        combine(allTracks, query) { tracks, q ->
            val filtered = if (q.isBlank()) {
            tracks
          } else {
                tracks.filter {
                (it.album ?: "").lowercase().contains(q.lowercase())
                }
          }

            groupTracksByAlbum(filtered)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
    val favorites: StateFlow<List<MusicEntity>> =
        repository.observeFavorites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recent: StateFlow<List<MusicEntity>> =
        repository.observeRecentlyPlayed()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        repository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchQuery: StateFlow<String> = query
    val selectedFilter: StateFlow<LibraryFilter> = filter

    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: LibraryFilter) {
        filter.value = value
    }

    fun scanDevice() {
        viewModelScope.launch {
            repository.scanDeviceToLibrary()
        }
    }

    fun toggleFavorite(track: MusicEntity) {
        viewModelScope.launch {
            repository.setFavorite(track.uri, !track.isFavorite)
        }
    }

    fun markPlayed(track: MusicEntity) {
        viewModelScope.launch {
            repository.markPlayed(track.uri)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addToPlaylist(playlistName: String, track: MusicEntity) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistName, track)
        }
    }

    fun renamePlaylist(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(oldName, newName)
        }
    }

    fun deletePlaylist(name: String) {
        viewModelScope.launch {
            repository.deletePlaylist(name)
        }
    }
}
