package com.rafa.musicas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.* // Mantém o Material3 que você está usando aqui
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // ADICIONADO
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafa.musicas.data.PlaylistStore

@Composable
fun PlaylistsScreen(store: PlaylistStore, onOpen: (String) -> Unit) {
    var playlists by remember { mutableStateOf(store.listPlaylists()) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { playlists = store.listPlaylists() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Playlists", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nova")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlists) { pl ->
                ElevatedCard(onClick = { onOpen(pl) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pl, style = MaterialTheme.typography.titleMedium)
                        Text("Abrir", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Criar playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nome") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = store.createPlaylist(newName.ifBlank { "Playlist" })
                    playlists = store.listPlaylists()
                    showCreate = false
                    newName = ""
                    onOpen(created)
                }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
        )
    }
}
