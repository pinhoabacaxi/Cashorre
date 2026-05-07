package com.rafa.musicas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
fun RecentScreen(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val recent by viewModel.recent.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tocadas recentemente", style = MaterialTheme.typography.titleLarge)
        Text("${recent.size} músicas recentes", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(12.dp))

        if (recent.isEmpty()) {
            Text("Nenhuma música tocada recentemente.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recent, key = { it.uri }) { track ->
                    RecentTrackRow(
                        track = track,
                        onPlay = {
                            PlayerManager.setQueueAndPlay(
                                context = context,
                                items = recent.map { it.toMediaItem() },
                                startIndex = recent.indexOf(track)
                            )
                            viewModel.markPlayed(track)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTrackRow(
    track: MusicEntity,
    onPlay: () -> Unit
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
        }
    }
}
