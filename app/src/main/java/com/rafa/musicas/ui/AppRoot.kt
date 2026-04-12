package com.rafa.musicas.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppRoot() {
    Text("App funcionando 🎵")
}
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.PlaybackService
import kotlinx.coroutines.launch

private object Routes {
    const val PLAYLISTS = "playlists"
    const val SEARCH = "search"
    const val PLAYLIST_DETAIL = "playlist/{name}"
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) { context.startService(Intent(context, PlaybackService::class.java)) }

    val store = remember { PlaylistStore(context) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Menu", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Músicas") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Routes.PLAYLISTS) { launchSingleTop = true }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Procurar músicas") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Routes.SEARCH) { launchSingleTop = true }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Músicas") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = { PlayerMiniBar() }
        ) { padding ->
            Box(Modifier.padding(padding)) {
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
}
