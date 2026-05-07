package com.rafa.musicas.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.rafa.musicas.data.MusicRepository
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.toMediaItem
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomPlaylistDetailViewModel(
    application: Application,
    playlistName: String
) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    val tracks = repository.observePlaylistTracks(playlistName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeTrack(playlistName: String, trackUri: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistName, trackUri)
        }
    }

    fun moveTrack(playlistName: String, currentTracks: List<MusicEntity>, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            repository.moveTrackInPlaylist(playlistName, currentTracks, fromIndex, toIndex)
        }
    }
    fun markPlayed(track: MusicEntity) {
        viewModelScope.launch {
            repository.markPlayed(track.uri)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPlaylistDetailScreen(
    playlistName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: RoomPlaylistDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RoomPlaylistDetailViewModel(
                    application = context.applicationContext as Application,
                    playlistName = playlistName
                )
            }
        }
    )

    val tracks by viewModel.tracks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(playlistName)
                        Text("${tracks.size} músicas", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (tracks.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Playlist vazia.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tracks, key = { _, item -> item.uri }) { index, track ->
                    RoomPlaylistTrackRow(
                        track = track,
                        onPlay = {
                            PlayerManager.setQueueAndPlay(
                                context = context,
                                items = tracks.map { it.toMediaItem() },
                                startIndex = index
                            )
                            viewModel.markPlayed(track)
                        },
                        onRemove = {
                            viewModel.removeTrack(playlistName, track.uri)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomPlaylistTrackRow(
    track: MusicEntity,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Tocar")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(track.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(track.author, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remover")
            }
        }
    }
}
