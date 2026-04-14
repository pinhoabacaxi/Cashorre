package com.rafa.musicas.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.PlaybackService

private object Routes {
    const val PLAYLISTS = "playlists"
    const val SEARCH = "search"
    const val PLAYLIST_DETAIL = "playlist/{name}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(store: PlaylistStore) { // Adicionado o parâmetro store
    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.startService(Intent(context, PlaybackService::class.java))
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashorre Player") },
                navigationIcon = {
                    IconButton(onClick = { /* Lógica do drawer aqui */ }) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = { PlayerMiniBar() }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            NavHost(navController = nav, startDestination = Routes.PLAYLISTS) {
                composable(Routes.PLAYLISTS) {
                    PlaylistsScreen(store = store, onOpen = { name -> nav.navigate("playlist/$name") })
                }
                composable(Routes.SEARCH) {
                    SearchAndImportScreen(store = store)
                }
                composable(
                    route = Routes.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("name") { type = NavType.StringType })
                ) { backStack ->
                    val name = backStack.arguments?.getString("name") ?: return@composable
                    PlaylistDetailScreen(playlistName = name, store = store, onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
