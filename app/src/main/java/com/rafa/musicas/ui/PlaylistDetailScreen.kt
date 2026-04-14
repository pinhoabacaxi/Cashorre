package com.rafa.musicas.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.* // MIGRADO PARA MATERIAL 3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.MetadataReader
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    store: PlaylistStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(playlistName) }
    var items by remember { mutableStateOf(store.listTrackItems(name)) }

    // Estados para diálogos
    var editDisplayFor by remember { mutableStateOf<String?>(null) }
    var deleteTrackFor by remember { mutableStateOf<String?>(null) }
    var showRenamePlaylist by remember { mutableStateOf(false) }
    var showDeletePlaylist by remember { mutableStateOf(false) }

    fun refresh() {
        items = store.listTrackItems(name)
    }

    Column(Modifier.fillMaxSize()) {
        // Cabeçalho
        SmallTopAppBar( // Componente Material 3
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
                IconButton(onClick = { showDeletePlaylist = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar")
                }
            }
        )

        HorizontalDivider() // No Material 3, Divider virou HorizontalDivider

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Playlist vazia", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(items) { index, item ->
                    TrackItemRow(
                        item = item,
                        onClick = {
                            PlayerManager.setQueueAndPlay(context, items, index)
                        },
                        onEdit = { editDisplayFor = item.mediaId },
                        onDelete = { deleteTrackFor = item.mediaId }
                    )
                }
            }
        }
    }

    // --- DIÁLOGOS (Todos atualizados para Material 3) ---

    if (showRenamePlaylist) {
        var newName by remember { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showRenamePlaylist = false },
            title = { Text("Renomear Playlist") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Novo nome") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (store.renamePlaylist(name, newName)) {
                        name = newName
                    }
                    showRenamePlaylist = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenamePlaylist = false }) { Text("Cancelar") }
            }
        )
    }

    if (editDisplayFor != null) {
        val fileName = editDisplayFor!!
        val current = items.find { it.mediaId == fileName }?.mediaMetadata?.displayTitle?.toString() ?: ""
        var value by remember { mutableStateOf(current) }
        AlertDialog(
            onDismissRequest = { editDisplayFor = null },
            title = { Text("Editar nome da faixa") },
            text = {
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Nome exibido") })
            },
            confirmButton = {
                TextButton(onClick = {
                    store.updateDisplayName(name, fileName, value)
                    editDisplayFor = null
                    refresh()
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editDisplayFor = null }) { Text("Cancelar") }
            }
        )
    }

    if (deleteTrackFor != null) {
        val fileName = deleteTrackFor!!
        AlertDialog(
            onDismissRequest = { deleteTrackFor = null },
            title = { Text("Remover música?") },
            text = { Text("Isto removerá a música da playlist.") },
            confirmButton = {
                TextButton(onClick = {
                    store.removeTrack(name, fileName, deleteFile = true)
                    deleteTrackFor = null
                    refresh()
                }) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTrackFor = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TrackItemRow(
    item: MediaItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard( // Componente Material 3
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.mediaMetadata.displayTitle?.toString() ?: "Desconhecido", style = MaterialTheme.typography.titleMedium)
                Text(item.mediaMetadata.artist?.toString() ?: "Autor desconhecido", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
        }
    }
}
