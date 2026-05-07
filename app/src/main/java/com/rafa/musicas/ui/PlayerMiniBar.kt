package com.rafa.musicas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.delay

@Composable
fun PlayerMiniBar(
    onOpenPlayer: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { PlayerManager.get(context) }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var title by remember { mutableStateOf("Nada tocando") }
    var artist by remember { mutableStateOf("Desconhecido") }
    var artworkUri by remember { mutableStateOf<String?>(null) }
    var volume by remember { mutableFloatStateOf(PlayerManager.getAppVolume()) }

    LaunchedEffect(Unit) {
        while (true) {
            isPlaying = player.isPlaying

            val metadata = player.currentMediaItem?.mediaMetadata
            title = metadata?.displayTitle?.toString() ?: "Nada tocando"
            artist = metadata?.artist?.toString() ?: "Desconhecido"
            artworkUri = metadata?.artworkUri?.toString()

            delay(1000)
        }
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPlayer() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkBox(
                    artworkUri = artworkUri,
                    size = 48.dp
                )

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2
                    )

                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                IconButton(onClick = { player.seekToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                }

                IconButton(
                    onClick = {
                        if (isPlaying) player.pause() else player.play()
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }

                IconButton(onClick = { player.seekToNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Próxima")
                }
            }

            Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    PlayerManager.setAppVolume(context, it)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
