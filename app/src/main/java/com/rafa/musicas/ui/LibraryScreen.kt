package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.db.PlaylistEntity
import com.rafa.musicas.data.toMediaItem
import com.rafa.musicas.player.PlayerManager

@Composable
fun LibraryScreen(
    onOpenArtists: () -> Unit = {},
    onOpenAlbums: () -> Unit = {},
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current

    val tracks by viewModel.tracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val scanStatus by viewModel.scanStatus.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

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
                Text(
                    text = "Biblioteca",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "${tracks.size} músicas",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = { viewModel.scanDevice() }) {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null
                )
                Text(" Escanear")
            }
        }

        scanStatus?.let { status ->
            Spacer(Modifier.height(8.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar música, artista ou álbum") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton(
                label = "Todas",
                selected = selectedFilter == LibraryFilter.ALL,
                onClick = { viewModel.setFilter(LibraryFilter.ALL) }
            )

            FilterChipButton(
                label = "Favoritas",
                selected = selectedFilter == LibraryFilter.FAVORITES,
                onClick = { viewModel.setFilter(LibraryFilter.FAVORITES) }
            )

            FilterChipButton(
                label = "Recentes",
                selected = selectedFilter == LibraryFilter.RECENT,
                onClick = { viewModel.setFilter(LibraryFilter.RECENT) }
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    text = "Nome",
                    selected = sortMode == LibrarySortMode.TITLE,
                    onClick = { viewModel.setSortMode(LibrarySortMode.TITLE) }
                )

                SortChip(
                    text = "Artista",
                    selected = sortMode == LibrarySortMode.ARTIST,
                    onClick = { viewModel.setSortMode(LibrarySortMode.ARTIST) }
                )

                SortChip(
                    text = "Álbum",
                    selected = sortMode == LibrarySortMode.ALBUM,
                    onClick = { viewModel.setSortMode(LibrarySortMode.ALBUM) }
                )

                 SortChip(
                    text = "Recentes",
                    selected = sortMode == LibrarySortMode.RECENT,
                    onClick = { viewModel.setSortMode(LibrarySortMode.RECENT) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenArtists,
                modifier = Modifier.weight(1f)
            ) {
                Text("Artistas")
            }

            Button(
                onClick = onOpenAlbums,
                modifier = Modifier.weight(1f)
            ) {
                Text("Álbuns")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (tracks.isEmpty()) {
            EmptyLibraryState(
                onScan = { viewModel.scanDevice() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = tracks,
                    key = { it.uri }
                ) { track ->
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
                        onFavorite = {
                            viewModel.toggleFavorite(track)
                        },
                        onAddToPlaylist = {
                            selectedTrack = track
                        },
                        onPlayNext = {
                            PlayerManager.playNext(
                                context,
                                track.toMediaItem()
                            )
                        },
                        onAddToQueue = {
                            PlayerManager.addToQueueEnd(
                                context,
                                track.toMediaItem()
                            )
                        }
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
            onCreatePlaylist = { showCreatePlaylist = true },
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
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                        }

                        newPlaylistName = ""
                        showCreatePlaylist = false
                    }
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreatePlaylist = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EmptyLibraryState(
    onScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LibraryMusic,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Nenhuma música encontrada",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Conceda permissão de áudio e escaneie o armazenamento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = onScan) {
            Text("Escanear músicas")
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
private fun LibraryTrackRow(
    track: MusicEntity,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkBox(
                    artworkUri = track.artworkUri,
                    size = 56.dp
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = track.author,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!track.album.isNullOrBlank()) {
                        Text(
                            text = track.album,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPlay) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Tocar"
                    )
                }

                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (track.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Favorito"
                    )
                }

                IconButton(onClick = onAddToPlaylist) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar à playlist"
                    )
                }

                IconButton(onClick = onPlayNext) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Tocar a seguir"
                    )
                }

                IconButton(onClick = onAddToQueue) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Adicionar à fila"
                    )
                }
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(track.displayName)

                if (playlists.isEmpty()) {
                    Text("Nenhuma playlist criada.")
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
