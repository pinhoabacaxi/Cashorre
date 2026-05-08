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

enum class LibrarySortMode {
    TITLE,
    ARTIST,
    ALBUM,
    RECENT
}
enum class LibraryFilter {
    ALL,
    FAVORITES,
    RECENT
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _sortMode =
        MutableStateFlow(LibrarySortMode.TITLE)

    val sortMode: StateFlow<LibrarySortMode> =
        _sortMode
    private val allTracks =
        repository.observeAllTracks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredTracks =
        combine(
            tracks,
            searchQuery,
            sortMode
        ) { tracksList, query, mode ->

            val filtered =
                if (query.isBlank()) {
                    tracksList
                } else {

                    val q = query.trim().lowercase()

                    tracksList.filter { track ->

                        track.displayName
                            .lowercase()
                            .contains(q)

                        ||

                        track.author
                            .lowercase()
                            .contains(q)

                        ||

                        (
                            track.album
                                ?.lowercase()
                                ?.contains(q)
                                == true
                        )
                    }
                }

            when (mode) {

                LibrarySortMode.TITLE -> {
                    filtered.sortedBy {
                        it.displayName.lowercase()
                    }
                }

                LibrarySortMode.ARTIST -> {
                     filtered.sortedBy {
                        it.author.lowercase()
                     }
                }

                LibrarySortMode.ALBUM -> {
                    filtered.sortedBy {
                        it.album?.lowercase() ?: ""
                    }
                }

                LibrarySortMode.RECENT -> {
                    filtered.sortedByDescending {
                        it.id
                    }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
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
    private val _scanStatus = MutableStateFlow<String?>(null)
    val scanStatus: StateFlow<String?> = _scanStatus


    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
    }
    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun setSortMode(mode: LibrarySortMode) {
         _sortMode.value = mode
    }
    fun setFilter(value: LibraryFilter) {
        filter.value = value
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
