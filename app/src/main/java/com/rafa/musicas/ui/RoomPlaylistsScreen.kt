package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoomPlaylistsScreen(
    onOpenPlaylist: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    val playlists by viewModel.playlists.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Playlists", style = MaterialTheme.typography.titleLarge)
                Text("${playlists.size} playlists", style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = {
                renameTarget = playlist.name
                renameText = playlist.name
            }) {
                Text("Renomear")
            }
            Button(onClick = onOpenLibrary) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null)
                Text(" Biblioteca")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (playlists.isEmpty()) {
            Text("Nenhuma playlist Room criada ainda. Abra a Biblioteca e adicione músicas.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlists, key = { it.name }) { playlist ->
                    ElevatedCard(
                        onClick = { onOpenPlaylist(playlist.name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                Text("Abrir playlist", style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(onClick = { onOpenPlaylist(playlist.name) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Abrir")
                            }

                            IconButton(onClick = { viewModel.deletePlaylist(playlist.name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            if (renameTarget != null) {
               AlertDialog(
                    onDismissRequest = { renameTarget = null },
                    title = { Text("Renomear playlist") },
                    text = {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.renamePlaylist(renameTarget!!, renameText)
                            renameTarget = null
                            renameText = ""
                        }) {
                           Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameTarget = null }) {
                        Text("Cancelar")
                      }
                  }
              )
            }
            }
        }
    }
}
