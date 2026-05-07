package com.rafa.musicas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.musicas.data.MusicRepository
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.db.PlaylistEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    val tracks: StateFlow<List<MusicEntity>> =
        repository.observeAllTracks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<MusicEntity>> =
        repository.observeFavorites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recent: StateFlow<List<MusicEntity>> =
        repository.observeRecentlyPlayed()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        repository.observePlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    fun deletePlaylist(name: String) {
    viewModelScope.launch {
        repository.deletePlaylist(name)
    }
}
}
