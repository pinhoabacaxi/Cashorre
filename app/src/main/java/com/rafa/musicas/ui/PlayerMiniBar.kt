package com.rafa.musicas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Mudado para Material3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.rafa.musicas.player.PlayerManager

@Composable
fun PlayerMiniBar() {
    val context = LocalContext.current
    val player = remember { PlayerManager.get(context) }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var title by remember { mutableStateOf("Nada tocando") }
    var volume by remember { mutableStateOf(PlayerManager.getAppVolume()) }

    // Surface do Material 3
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = { player.seekToPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null)
                    }
                    IconButton(onClick = { if (isPlaying) player.pause() else player.play() }) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    }
                    IconButton(onClick = { player.seekToNext() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
                    }
                }
                Slider(
                    value = volume,
                    onValueChange = { 
                        volume = it
                        PlayerManager.setAppVolume(context, it) 
                    },
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}
