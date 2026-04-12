package com.rafa.musicas.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafa.musicas.data.PlaylistStore

@Composable
fun SearchAndImportScreen(store: PlaylistStore) {
    val context = LocalContext.current

    var selectedTree by remember { mutableStateOf<Uri?>(null) }
    var playlists by remember { mutableStateOf(store.listPlaylists()) }
    var selectedPlaylist by remember { mutableStateOf(playlists.firstOrNull()) }
    var newPlaylistName by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Selecione uma pasta para importar .mp3/.m4a") }
    var recursive by remember { mutableStateOf(true) }

    val pickFolder = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = (android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
            selectedTree = uri
            status = "Pasta selecionada. Agora escolha/crie a playlist e toque em Importar."
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Procurar músicas", style = MaterialTheme.typography.titleLarge)

        ElevatedCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(status)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { pickFolder.launch(null) }) { Text("Selecionar pasta") }
                    OutlinedButton(
                        enabled = selectedTree != null,
                        onClick = {
                            val pl = if (newPlaylistName.isNotBlank()) {
                                store.createPlaylist(newPlaylistName)
                            } else {
                                selectedPlaylist ?: store.createPlaylist("Playlist")
                            }
                            val copied = store.addTracksFromTree(selectedTree!!, pl, recursive = recursive)
                            playlists = store.listPlaylists()
                            selectedPlaylist = pl
                            newPlaylistName = ""
                            status = "Importadas $copied músicas para "$pl"."
                        }
                    ) { Text("Importar") }
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = recursive, onCheckedChange = { recursive = it })
                    Spacer(Modifier.width(6.dp))
                    Text("Incluir subpastas (recursivo)")
                }
            }
        }

        ElevatedCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Escolher playlist destino", style = MaterialTheme.typography.titleMedium)

                if (playlists.isEmpty()) {
                    Text("Nenhuma playlist ainda. Crie uma abaixo.")
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedPlaylist ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Playlist existente") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            playlists.forEach { pl ->
                                DropdownMenuItem(
                                    text = { Text(pl) },
                                    onClick = {
                                        selectedPlaylist = pl
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Ou criar nova playlist (nome)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "O app copia os arquivos para as pastas internas em /midias/<playlist>/.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
