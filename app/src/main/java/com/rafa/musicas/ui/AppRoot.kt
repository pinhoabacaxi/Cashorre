package com.rafa.musicas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.* // Para Scaffold, TopAppBar e Text (Material 2)
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.rafa.musicas.data.PlaylistStore

@Composable
fun AppRoot(store: PlaylistStore) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Playlists) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cashorre Player") })
        }
    ) { innerPadding ->
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
        
        // Se o seu player bar fica fixo embaixo, ele entra aqui:
        PlayerMiniBar() 
    }
}

// Classe selada para controlar a navegação sem erros de digitação
sealed class Screen {
    object Playlists : Screen()
    data class Details(val name: String) : Screen()
    object Import : Screen()
}
