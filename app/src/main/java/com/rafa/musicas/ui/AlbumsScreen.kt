package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafa.musicas.data.db.MusicEntity
import com.rafa.musicas.data.toMediaItem
import com.rafa.musicas.player.PlayerManager

@Composable
fun AlbumsScreen(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val tracks by viewModel.tracks.collectAsState()

    val albums: List<Pair<String, List<MusicEntity>>> =
        tracks
            .groupBy { it.album?.ifBlank { "Sem álbum" } ?: "Sem álbum" }
            .toList()
            .sortedBy { it.first.lowercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Álbuns", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (albums.isEmpty()) {
            Text("Nenhum álbum encontrado.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(albums, key = { it.first }) { albumGroup ->
                    val albumName = albumGroup.first
                    val albumTracks = albumGroup.second
                    val artworkUri = albumTracks.firstOrNull()?.artworkUri

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            PlayerManager.setQueueAndPlay(
                                context = context,
                                items = albumTracks.map { it.toMediaItem() },
                                startIndex = 0
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtworkBox(
                                artworkUri = artworkUri,
                                size = 56.dp
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = albumName,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2
                                )
                                Text(
                                    text = "${albumTracks.size} músicas",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(
                                onClick = {
                                    PlayerManager.setQueueAndPlay(
                                        context = context,
                                        items = albumTracks.map { it.toMediaItem() },
                                        startIndex = 0
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Tocar álbum"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
