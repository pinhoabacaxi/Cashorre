package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafa.musicas.player.PlayerManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var volume by remember {
        mutableFloatStateOf(PlayerManager.getAppVolume())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.titleLarge
        )

        Divider()

        Text(
            text = "Volume do app",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall
        )

        Slider(
            value = volume,
            onValueChange = {
                volume = it
                PlayerManager.setAppVolume(context, it)
            }
        )

        Divider()

        Text(
            text = "DSP, equalizador e processamento avançado de áudio não fazem parte deste app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
