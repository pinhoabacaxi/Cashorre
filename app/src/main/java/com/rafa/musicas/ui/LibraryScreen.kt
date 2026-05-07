package com.rafa.musicas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.db.PlaylistEntity
import com.rafa.musicas.data.toMediaItem
import com.rafa.musicas.player.PlayerManager

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val tracks by viewModel.tracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    var selectedTrack by remember { mutableStateOf<MusicEntity?>(null) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Biblioteca", style = MaterialTheme.typography.titleLarge)
                Text("${tracks.size} músicas", style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = { viewModel.scanDevice() }) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null)
                Text(" Escanear")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (tracks.isEmpty()) {
            Text("Nenhuma música encontrada. Toque em Escanear para buscar músicas no dispositivo.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tracks, key = { it.uri }) { track ->
                    LibraryTrackRow(
                        track = track,
                        onPlay = {
                            PlayerManager.setQueueAndPlay(
                                context = context,
                                items = tracks.map { it.toMediaItem() },
                                startIndex = tracks.indexOf(track)
                            )
                            viewModel.markPlayed(track)
                        },
                        onFavorite = { viewModel.toggleFavorite(track) },
                        onAddToPlaylist = { selectedTrack = track }
                    )
                }
            }
        }
    }

    selectedTrack?.let { track ->
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { selectedTrack = null },
            onCreatePlaylist = {
                showCreatePlaylist = true
            },
            onAdd = { playlistName ->
                viewModel.addToPlaylist(playlistName, track)
                selectedTrack = null
            }
        )
    }

    if (showCreatePlaylist) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylist = false },
            title = { Text("Nova playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nome da playlist") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        viewModel.createPlaylist(newPlaylistName)
                    }
                    newPlaylistName = ""
                    showCreatePlaylist = false
                }) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylist = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun LibraryTrackRow(
    track: MusicEntity,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Tocar")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(track.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(track.author, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito"
                )
            }

            IconButton(onClick = onAddToPlaylist) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar à playlist")
            }
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    track: MusicEntity,
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onAdd: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar à playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(track.displayName, style = MaterialTheme.typography.bodyMedium)

                if (playlists.isEmpty()) {
                    Text("Nenhuma playlist Room criada ainda.")
                } else {
                    playlists.forEach { playlist ->
                        Button(
                            onClick = { onAdd(playlist.name) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(playlist.name)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onCreatePlaylist,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Criar nova playlist")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
