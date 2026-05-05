@Composable
fun PlaylistsScreen(
    store: PlaylistStore,
    onOpen: (String) -> Unit,
    onImport: () -> Unit
) {

    var playlists by remember { mutableStateOf(store.listPlaylists()) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        playlists = store.listPlaylists()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Playlists", style = MaterialTheme.typography.titleLarge)

            Row {
                Button(onClick = onImport) {
                    Text("Importar")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { showCreate = true }) {
                    Text("Nova")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlists) { pl ->
                ElevatedCard(
                    onClick = { onOpen(pl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pl)
                        Text("Abrir")
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
                    onValueChange = { newName = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = store.createPlaylist(newName.ifBlank { "Playlist" })
                    playlists = store.listPlaylists()
                    showCreate = false
                    newName = ""
                    onOpen(created)
                }) {
                    Text("Criar")
                }
            }
        )
    }
}
