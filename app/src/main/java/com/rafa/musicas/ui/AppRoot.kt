package com.rafa.musicas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.rafa.musicas.data.PlaylistStore

@Composable
fun AppRoot(store: PlaylistStore) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Playlists) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cashorre Player") })
        },
        bottomBar = {
            PlayerMiniBar()
        }
    ) { innerPadding ->
        // O Box PRECISA estar dentro das chaves do Scaffold para receber o padding
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val s = currentScreen) {
                is Screen.Playlists -> PlaylistsScreen(
                    store = store, 
                    onOpen = { currentScreen = Screen.Details(it) }
                )
                is Screen.Details -> PlaylistDetailScreen(
                    playlistName = s.name, 
                    store = store, 
                    onBack = { currentScreen = Screen.Playlists }
                )
                is Screen.Import -> SearchAndImportScreen(store = store)
            }
        }
    }
}

sealed class Screen {
    object Playlists : Screen()
    data class Details(val name: String) : Screen()
    object Import : Screen()
}
