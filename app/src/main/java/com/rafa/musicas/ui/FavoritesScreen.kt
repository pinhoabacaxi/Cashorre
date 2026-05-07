package com.rafa.musicas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.toMediaItem
import com.rafa.musicas.player.PlayerManager

@Composable
fun FavoritesScreen(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Favoritos", style = MaterialTheme.typography.titleLarge)
        Text("${favorites.size} músicas favoritas", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(12.dp))

        if (favorites.isEmpty()) {
            Text("Nenhuma música favorita ainda.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favorites, key = { it.uri }) { track ->
                    FavoriteTrackRow(
                        track = track,
                        onPlay = {
                            PlayerManager.setQueueAndPlay(
                                context = context,
                                items = favorites.map { it.toMediaItem() },
                                startIndex = favorites.indexOf(track)
                            )
                            viewModel.markPlayed(track)
                        },
                        onRemoveFavorite = {
                            viewModel.toggleFavorite(track)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTrackRow(
    track: MusicEntity,
    onPlay: () -> Unit,
    onRemoveFavorite: () -> Unit
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

            IconButton(onClick = onRemoveFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = "Remover dos favoritos")
            }
        }
    }
}
