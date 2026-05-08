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

enum class LibraryFilter {
    ALL,
    FAVORITES,
    RECENT
}

enum class LibrarySortMode {
    TITLE,
    ARTIST,
    ALBUM,
    RECENT
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    private val allTracks: StateFlow<List<MusicEntity>> =
        repository.observeAllTracks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.ALL)
    private val sort = MutableStateFlow(LibrarySortMode.TITLE)
    private val _scanStatus = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = query
    val selectedFilter: StateFlow<LibraryFilter> = filter
    val sortMode: StateFlow<LibrarySortMode> = sort
    val scanStatus: StateFlow<String?> = _scanStatus

    val tracks: StateFlow<List<MusicEntity>> =
        combine(allTracks, query, filter, sort) { source, search, selectedFilter, selectedSort ->
            val filteredByMode = when (selectedFilter) {
                LibraryFilter.ALL -> source
                LibraryFilter.FAVORITES -> source.filter { it.isFavorite }
                LibraryFilter.RECENT -> source
                    .filter { it.lastPlayedAt != null }
                    .sortedByDescending { it.lastPlayedAt ?: 0L }
            }

            val q = search.trim().lowercase()

            val filteredBySearch = if (q.isBlank()) {
                filteredByMode
            } else {
                filteredByMode.filter { track ->
                    track.displayName.lowercase().contains(q) ||
                        track.author.lowercase().contains(q) ||
                        (track.album ?: "").lowercase().contains(q)
                }
            }

            when (selectedSort) {
                LibrarySortMode.TITLE -> filteredBySearch.sortedBy { it.displayName.lowercase() }
                LibrarySortMode.ARTIST -> filteredBySearch.sortedBy { it.author.lowercase() }
                LibrarySortMode.ALBUM -> filteredBySearch.sortedBy { it.album?.lowercase() ?: "" }
                LibrarySortMode.RECENT -> filteredBySearch.sortedByDescending { it.lastPlayedAt ?: it.addedAt }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<MusicEntity>> =
        repository.observeFavorites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recent: StateFlow<List<MusicEntity>> =
        repository.observeRecentlyPlayed()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        repository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: LibraryFilter) {
        filter.value = value
    }

    fun setSortMode(value: LibrarySortMode) {
        sort.value = value
    }

    fun scanDevice() {
        viewModelScope.launch {
            _scanStatus.value = "Escaneando músicas..."
            val count = repository.scanDeviceToLibrary()
            _scanStatus.value = if (count > 0) {
                "$count músicas encontradas/atualizadas"
            } else {
                "Nenhuma música nova encontrada"
            }
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
