package com.rafa.musicas.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBack // Exemplo comum
import androidx.compose.material.icons.filled.MoreVert // Exemplo comum
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.media3.common.Player
import com.rafa.musicas.player.PlayerManager
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

@Composable
fun PlayerMiniBar() {
    val context = LocalContext.current
    val player = remember { PlayerManager.get(context) }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var title by remember { mutableStateOf("Nada tocando") }
    var artist by remember { mutableStateOf<String?>(null) }
    var positionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var volume by remember { mutableStateOf(PlayerManager.getAppVolume()) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNew: Boolean) {
                isPlaying = isPlayingNew
            }

            override fun onMediaItemTransition(
                mediaItem: androidx.media3.common.MediaItem?,
                reason: Int
            ) {
                val md = mediaItem?.mediaMetadata
                title = md?.title?.toString()
                    ?: (mediaItem?.localConfiguration?.uri?.lastPathSegment ?: "Tocando")
                artist = md?.artist?.toString()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = player.duration.coerceAtLeast(0L)
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            positionMs = player.currentPosition
            durationMs = player.duration.coerceAtLeast(0L)
            delay(250)
        }
    }

    Surface(shadowElevation = 10.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )

            if (!artist.isNullOrBlank()) {
                Text(
                    artist!!,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            val dur = durationMs
            val pos = positionMs.coerceIn(0L, if (dur > 0) dur else Long.MAX_VALUE)

            Slider(
                value = if (dur > 0) (pos.toFloat() / dur.toFloat()) else 0f,
                onValueChange = { frac ->
                    if (dur > 0) player.seekTo((dur * frac).roundToLong())
                }
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = { player.seekToPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                    }

                    IconButton(onClick = {
                        if (isPlaying) player.pause() else player.play()
                    }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }

                    IconButton(onClick = { player.seekToNext() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Próxima")
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)

                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            PlayerManager.setAppVolume(context, it)
                        },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}
