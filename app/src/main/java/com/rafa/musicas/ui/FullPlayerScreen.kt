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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.delay
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onBack: () -> Unit,
    onOpenQueue: () -> Unit
) {

    val context = LocalContext.current
    val player = remember {
        PlayerManager.get(context)
    }

    var isPlaying by remember {
        mutableStateOf(player.isPlaying)
    }

    var title by remember {
        mutableStateOf("Nada tocando")
    }

    var artist by remember {
        mutableStateOf("Desconhecido")
    }

    var artworkUri by remember {
        mutableStateOf<String?>(null)
    }

    var position by remember {
        mutableLongStateOf(0L)
    }

    var duration by remember {
        mutableLongStateOf(0L)
    }

    var sliderPosition by remember {
        mutableFloatStateOf(0f)
    }

    var isUserSeeking by remember {
        mutableStateOf(false)
    }

    var shuffleEnabled by remember {
        mutableStateOf(player.shuffleModeEnabled)
    }

    var repeatEnabled by remember {
        mutableStateOf(player.repeatMode != Player.REPEAT_MODE_OFF)
    }

    LaunchedEffect(Unit) {

        while (true) {

            isPlaying = player.isPlaying

            val metadata = player.currentMediaItem?.mediaMetadata

            title = metadata?.displayTitle?.toString()
                ?: "Nada tocando"

            artist = metadata?.artist?.toString()
                ?: "Desconhecido"

            artworkUri = metadata?.artworkUri?.toString()

            shuffleEnabled = player.shuffleModeEnabled

            repeatEnabled =
                player.repeatMode != Player.REPEAT_MODE_OFF

            position = player.currentPosition
                .coerceAtLeast(0L)

            duration =
                if (player.duration > 0) {
                    player.duration
                } else {
                    0L
                }

            if (!isUserSeeking && duration > 0) {
                sliderPosition =
                    position.toFloat() / duration.toFloat()
            }

            PlayerManager.saveQueue(context)

            delay(2000)
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Reproduzindo")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = onOpenQueue
                    ) {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = "Fila"
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
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {

            ArtworkBox(
                artworkUri = artworkUri,
                size = 280.dp
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(Modifier.height(24.dp))

            Slider(
                value = sliderPosition,

                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },

                onValueChangeFinished = {

                    if (duration > 0) {
                        player.seekTo(
                            (sliderPosition * duration).toLong()
                        )
                    }

                    isUserSeeking = false
                },

                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    formatDuration(position),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    formatDuration(duration),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceEvenly,

                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {

                        player.shuffleModeEnabled =
                            !player.shuffleModeEnabled

                        shuffleEnabled =
                            player.shuffleModeEnabled
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",

                        tint =
                            if (shuffleEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                    )
                }

                IconButton(
                    onClick = {
                        player.seekToPrevious()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = {

                        if (isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                ) {

                    Icon(
                        imageVector =
                            if (isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },

                        contentDescription = "Play/Pause",

                        modifier = Modifier.size(64.dp)
                    )
                }

                IconButton(
                    onClick = {
                        player.seekToNext()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = {

                        player.repeatMode =
                            if (
                                player.repeatMode ==
                                Player.REPEAT_MODE_OFF
                            ) {
                                Player.REPEAT_MODE_ALL
                            } else {
                                Player.REPEAT_MODE_OFF
                            }

                        repeatEnabled =
                            player.repeatMode !=
                                Player.REPEAT_MODE_OFF
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",

                        tint =
                            if (repeatEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {

    val totalSeconds = max(
        0,
        ms / 1000
    )

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%d:%02d".format(
        minutes,
        seconds
    )
}
