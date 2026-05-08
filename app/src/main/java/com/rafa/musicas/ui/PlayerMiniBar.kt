package com.rafa.musicas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.delay

@Composable
fun PlayerMiniBar(
    player: Player,
    onExpand: () -> Unit
) {

    var isPlaying by remember {
        mutableStateOf(player.isPlaying)
    }

    var progress by remember {
        mutableFloatStateOf(0f)
    }

    var currentPosition by remember {
        mutableLongStateOf(0L)
    }

    var duration by remember {
        mutableLongStateOf(1L)
    }

    var repeatOne by remember {
        mutableStateOf(
            player.repeatMode == Player.REPEAT_MODE_ONE
        )
    }

    LaunchedEffect(player) {

        while (true) {

            isPlaying = player.isPlaying

            currentPosition =
                player.currentPosition.coerceAtLeast(0L)

            duration =
                player.duration
                    .takeIf { it > 0L }
                    ?: 1L

            progress =
                (currentPosition.toFloat() / duration.toFloat())
                    .coerceIn(0f, 1f)

            repeatOne =
                player.repeatMode == Player.REPEAT_MODE_ONE

            delay(500)
        }
    }

    val currentMediaItem = player.currentMediaItem

    if (currentMediaItem == null) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(18.dp)
            )
            .clickable {
                onExpand()
            }
    ) {

        Column {

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                ArtworkBox(
                    artworkUri =
                        currentMediaItem.mediaMetadata.artworkUri
                            ?.toString(),

                    size = 54.dp
                )

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text =
                            currentMediaItem.mediaMetadata.title
                                ?.toString()
                                ?: "Desconhecido",

                        style =
                            MaterialTheme.typography.titleSmall,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text =
                            currentMediaItem.mediaMetadata.artist
                                ?.toString()
                                ?: "Artista desconhecido",

                        style =
                            MaterialTheme.typography.bodySmall,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {

                            player.seekToPreviousMediaItem()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.SkipPrevious,

                            contentDescription = "Anterior"
                        )
                    }

                    IconButton(
                        onClick = {

                            if (player.isPlaying) {
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

                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = {

                            player.seekToNextMediaItem()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.SkipNext,

                            contentDescription = "Próxima"
                        )
                    }

                    IconButton(
                        onClick = {

                            player.repeatMode =
                                if (
                                    player.repeatMode ==
                                    Player.REPEAT_MODE_ONE
                                ) {
                                    Player.REPEAT_MODE_OFF
                                } else {
                                    Player.REPEAT_MODE_ONE
                                }

                            repeatOne =
                                player.repeatMode ==
                                    Player.REPEAT_MODE_ONE
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.RepeatOne,

                            contentDescription =
                               "Repetir música",

                            tint =
                                if (repeatOne) {
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
}
