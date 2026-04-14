package com.rafa.musicas.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafa.musicas.data.PlaylistStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndImportScreen(store: PlaylistStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var playlists by remember { mutableStateOf(store.listPlaylists()) }
    var selectedPlaylist by remember { mutableStateOf(playlists.firstOrNull()) }
    var newPlaylistName by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Selecione uma pasta para importar músicas") }
    var recursive by remember { mutableStateOf(true) }
    var isImporting by remember { mutableStateOf(false) }

    val pickFolder = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // CORREÇÃO LINT: takePersistableUriPermission aceita apenas flags de acesso.
            // FLAG_GRANT_PERSISTABLE_URI_PERMISSION não deve ser passada aqui.
            val contentResolver = context.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            scope.launch {
                isImporting = true
                status = "Importando músicas... Por favor, aguarde."
                
                try {
                    val targetPlaylist = if (newPlaylistName.isNotBlank()) {
                        store.createPlaylist(newPlaylistName)
                    } else {
                        selectedPlaylist ?: "Playlist"
                    }

                    // CORREÇÃO DE TIPO E ORDEM: 
                    // store.addTracksFromTree(playlistName: String, treeUri: Uri, recursive: Boolean)
                    val count = withContext(Dispatchers.IO) {
                        store.addTracksFromTree(
                            playlistName = targetPlaylist, 
                            treeUri = uri, 
                            recursive = recursive
                        )
                    }

                    status = "Sucesso: $count músicas adicionadas a '$targetPlaylist'"
                    playlists = store.listPlaylists()
                    newPlaylistName = ""
                } catch (e: Exception) {
                    status = "Erro: ${e.localizedMessage ?: "Falha na importação"}"
                } finally {
                    isImporting = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Importar da Memória", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Configuração", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = recursive, onCheckedChange = { recursive = it })
                    Text("Incluir subpastas")
                }

                if (playlists.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedPlaylist ?: "Selecione uma playlist",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Destino") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
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
                    label = { Text("Ou criar nova playlist") },
                    placeholder = { Text("Ex: Favoritas do Verão") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Button(
            onClick = { pickFolder.launch(null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), 
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Selecionar Pasta e Iniciar")
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = status,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
