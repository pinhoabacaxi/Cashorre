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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoomPlaylistsScreen(
    onOpenPlaylist: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {

    val playlists by viewModel.playlists.collectAsState()

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var playlistName by remember {
        mutableStateOf("")
    }

    Scaffold(

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showCreateDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nova playlist"
                )
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "Playlists",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = onOpenLibrary
                ) {
                    Text("Biblioteca")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (playlists.isEmpty()) {

                Text("Nenhuma playlist criada.")

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(playlists) { playlist ->

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onOpenPlaylist(playlist.name)
                            }
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    playlist.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {

        AlertDialog(

            onDismissRequest = {
                showCreateDialog = false
            },

            title = {
                Text("Nova playlist")
            },

            text = {

                OutlinedTextField(
                    value = playlistName,

                    onValueChange = {
                        playlistName = it
                    },

                    label = {
                        Text("Nome")
                    },

                    singleLine = true
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (playlistName.isNotBlank()) {

                            viewModel.createPlaylist(
                                playlistName
                            )
                        }

                        playlistName = ""
                        showCreateDialog = false
                    }
                ) {
                    Text("Criar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCreateDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
