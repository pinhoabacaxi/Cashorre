package com.rafa.musicas.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.MetadataReader
import com.rafa.musicas.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    store: PlaylistStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(playlistName) }
    var items by remember { mutableStateOf(store.listTrackItems(name)) }

    var showRename by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(name) }
    var showDelete by remember { mutableStateOf(false) }

    var editDisplayFor by remember { mutableStateOf<Pair<String, String>?>(null) } // (fileName, currentDisplay)
    var deleteTrackFor by remember { mutableStateOf<String?>(null) }

    fun refresh() { items = store.listTrackItems(name) }

    // Cache metadata in memory for UI and queue building
    val metaCache = remember { mutableStateMapOf<String, com.rafa.musicas.player.TrackMeta>() }

    suspend fun ensureMeta(file: File) {
        val key = file.absolutePath
        if (metaCache.containsKey(key)) return
        val meta = withContext(Dispatchers.IO) { MetadataReader.read(context, file) }
        metaCache[key] = meta
        // Persist title/artist/duration into order.json (optional)
        val entry = items.firstOrNull { it.file.absolutePath == key }?.entry
        if (entry != null && (entry.title == null || entry.artist == null || entry.durationMs == null)) {
            val idx = store.readIndex(name)
            val updated = idx.tracks.map {
                if (it.fileName == entry.fileName) it.copy(
                    title = meta.title,
                    artist = meta.artist,
                    durationMs = meta.durationMs
                ) else it
            }
            store.writeIndex(name, idx.copy(tracks = updated))
            refresh()
        }
    }

    LaunchedEffect(name) {
        refresh()
        // Preload metadata lazily (first few)
        items.take(12).forEach { ensureMeta(it.file) }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                Column(Modifier.padding(top = 6.dp)) {
                    Text(name, style = MaterialTheme.typography.titleLarge)
                    Text("${items.size} músicas", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = { showRename = true }) { Icon(Icons.Default.Edit, contentDescription = "Renomear") }
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "Apagar playlist") }
            }
        }

        Divider()

        Text(
            "Dica: pressione e segure uma música para arrastar e reordenar.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Simple drag & drop reorder within LazyColumn
        var draggingIndex by remember { mutableStateOf<Int?>(null) }
        var dragOffset by remember { mutableStateOf(0f) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.file.name }) { idx, item ->
                val file = item.file
                val entry = item.entry
                val metaKey = file.absolutePath
                val meta = metaCache[metaKey]

                LaunchedEffect(metaKey) { ensureMeta(file) }

                val isDragging = draggingIndex == idx
                val elevation = if (isDragging) 20.dp else 2.dp

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(items, idx) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = idx
                                    dragOffset = 0f
                                },
                                onDragEnd = {
                                    draggingIndex = null
                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y
                                    val from = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                    // Heuristic: swap when dragged beyond ~half item height
                                    val threshold = 80f
                                    if (dragOffset > threshold && from < items.lastIndex) {
                                        store.reorder(name, from, from + 1)
                                        refresh()
                                        draggingIndex = from + 1
                                        dragOffset = 0f
                                    } else if (dragOffset < -threshold && from > 0) {
                                        store.reorder(name, from, from - 1)
                                        refresh()
                                        draggingIndex = from - 1
                                        dragOffset = 0f
                                    }
                                }
                            )
                        },
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                    onClick = {
                        val mediaItems = items.map { ti ->
                            val f = ti.file
                            val mk = f.absolutePath
                            val m = metaCache[mk]
                            val md = MediaMetadata.Builder()
                                .setTitle(m?.title ?: ti.entry.displayName)
                                .setArtist(m?.artist)
                                .build()
                            MediaItem.Builder()
                                .setUri(f.toURI().toString())
                                .setMediaMetadata(md)
                                .build()
                        }
                        PlayerManager.setQueueAndPlay(context, mediaItems, startIndex = idx)
                    }
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(Modifier.weight(1f)) {
                            // Cover
                            val coverPath = meta?.coverPath
                            if (coverPath != null) {
                                val bmp = remember(coverPath) {
                                    runCatching { BitmapFactory.decodeFile(coverPath) }.getOrNull()
                                }
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }
                            } else {
                                Box(
                                    Modifier.size(54.dp).background(Color(0xFFEAEAEA))
                                )
                                Spacer(Modifier.width(12.dp))
                            }

                            Column {
                                Text(entry.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                val subtitle = listOfNotNull(meta?.artist, meta?.title).filter { it.isNotBlank() }
                                if (subtitle.isNotEmpty()) {
                                    Text(subtitle.joinToString(" • "), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                } else {
                                    Text("Toque para tocar", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Row {
                            IconButton(onClick = { editDisplayFor = entry.fileName to entry.displayName }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar nome")
                            }
                            IconButton(onClick = { deleteTrackFor = entry.fileName }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Renomear playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Novo nome") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val renamed = store.renamePlaylist(name, newName)
                    name = renamed
                    showRename = false
                    refresh()
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text("Cancelar") } }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Apagar playlist?") },
            text = { Text("Isso remove a pasta e todas as músicas copiadas para ela.") },
            confirmButton = {
                TextButton(onClick = {
                    store.deletePlaylist(name)
                    showDelete = false
                    onBack()
                }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
        )
    }

    if (editDisplayFor != null) {
        val (fileName, current) = editDisplayFor!!
        var value by remember { mutableStateOf(current) }
        AlertDialog(
            onDismissRequest = { editDisplayFor = null },
            title = { Text("Editar nome da faixa") },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Nome exibido") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.updateDisplayName(name, fileName, value)
                    editDisplayFor = null
                    refresh()
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { editDisplayFor = null }) { Text("Cancelar") } }
        )
    }

    if (deleteTrackFor != null) {
        val fileName = deleteTrackFor!!
        AlertDialog(
            onDismissRequest = { deleteTrackFor = null },
            title = { Text("Remover música?") },
            text = { Text("Removerá a música desta playlist (e apagará o arquivo copiado).") },
            confirmButton = {
                TextButton(onClick = {
                    store.removeTrack(name, fileName, deleteFile = true)
                    deleteTrackFor = null
                    refresh()
                }) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { deleteTrackFor = null }) { Text("Cancelar") } }
        )
    }
}
