package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { PlayerManager.get(context) }

    var queue by remember {
        mutableStateOf(PlayerManager.getQueue(context))
    }

    var currentIndex by remember {
        mutableStateOf(player.currentMediaItemIndex)
    }

    LaunchedEffect(Unit) {
        while (true) {
            queue = PlayerManager.getQueue(context)
            currentIndex = player.currentMediaItemIndex
            delay(500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Fila")
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "${queue.size} músicas na fila",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            if (queue.isEmpty()) {

                Text("A fila está vazia.")

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    itemsIndexed(queue) { index, item ->

                        QueueRow(
                            item = item,

                            isCurrent = index == currentIndex,

                            onPlay = {
                                player.seekTo(index, 0L)
                                player.playWhenReady = true
                            },

                            onMoveUp = {
                                PlayerManager.moveQueueItem(
                                    context,
                                    index,
                                    index - 1
                                )
                            },

                            onMoveDown = {
                                PlayerManager.moveQueueItem(
                                    context,
                                    index,
                                    index + 1
                                )
                            },

                            onRemove = {
                                PlayerManager.removeFromQueue(
                                    context,
                                    index
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: MediaItem,
    isCurrent: Boolean,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {

    val metadata = item.mediaMetadata

    val title =
        metadata.displayTitle?.toString()
            ?: "Sem título"

    val artist =
        metadata.artist?.toString()
            ?: "Desconhecido"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            IconButton(
                onClick = onPlay
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Tocar",

                    tint =
                        if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text =
                        if (isCurrent) {
                            "▶ $title"
                        } else {
                            title
                        },

                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )

                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onMoveUp
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Subir"
                )
            }

            IconButton(
                onClick = onMoveDown
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Descer"
                )
            }

            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover"
                )
            }
        }
    }
}
