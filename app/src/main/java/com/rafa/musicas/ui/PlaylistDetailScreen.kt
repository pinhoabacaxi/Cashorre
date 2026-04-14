package com.rafa.musicas.ui

import com.rafa.musicas.data.TrackItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.* // Totalmente Material 3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.PlayerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    store: PlaylistStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(playlistName) }
    // O tipo aqui é List<PlaylistStore.TrackItem>
    var items by remember { mutableStateOf(store.listTrackItems(name)) }

    var editDisplayFor by remember { mutableStateOf<String?>(null) }
    var deleteTrackFor by remember { mutableStateOf<String?>(null) }
    var showRenamePlaylist by remember { mutableStateOf(false) }

    fun refresh() {
        items = store.listTrackItems(name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        Text("${items.size} músicas", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showRenamePlaylist = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Renomear")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Playlist vazia")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(items) { index, item ->
                        // Passamos o item do tipo TrackItem para a Row
                        TrackItemRow(
                            item = item,
                            onClick = {
                                // CORREÇÃO: Mapeia a lista de TrackItem para MediaItem antes de tocar
                                val mediaItems = items.map { it.toMediaItem() }
                                PlayerManager.setQueueAndPlay(context, mediaItems, index)
                            },
                            onEdit = { editDisplayFor = item.fileName }, // TrackItem usa fileName
                            onDelete = { deleteTrackFor = item.fileName }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Deleção (Correção do erro de Boolean vs String)
    if (deleteTrackFor != null) {
        val fileToDelete = deleteTrackFor!!
        AlertDialog(
            onDismissRequest = { deleteTrackFor = null },
            title = { Text("Remover música?") },
            text = { Text("Deseja remover esta música da playlist?") },
            confirmButton = {
                TextButton(onClick = {
                    // Certifique-se que a ordem é: (nomePlaylist, nomeArquivo, booleano)
                    store.removeTrack(name, fileToDelete, true) 
                    deleteTrackFor = null
                    refresh()
                }) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTrackFor = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Renomear
    if (showRenamePlaylist) {
        var newName by remember { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showRenamePlaylist = false },
            title = { Text("Renomear Playlist") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (store.renamePlaylist(name, newName)) {
                        name = newName
                    }
                    showRenamePlaylist = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenamePlaylist = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TrackItemRow(
    item: TrackItem, // Agora o import acima resolve isso
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                // TrackItem usa 'displayName' em vez de 'mediaMetadata.displayTitle'
                Text(item.displayName, style = MaterialTheme.typography.titleMedium)
                Text(item.author, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
        }
    }
}
