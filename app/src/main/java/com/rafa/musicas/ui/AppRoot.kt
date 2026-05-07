package com.rafa.musicas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(store: PlaylistStore) {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = {
            PlayerMiniBar(
                onOpenPlayer = {
                    nav.navigate("player")
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "room_playlists",
            modifier = Modifier.padding(padding)
        ) {
            composable("room_playlists") {
                RoomPlaylistsScreen(
                    onOpenPlaylist = { playlist ->
                        nav.navigate("room_playlist/$playlist")
                    },
                    onOpenLibrary = {
                        nav.navigate("library")
                    }
                )
            }

            composable(
                route = "room_playlist/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { entry ->
                val name = entry.arguments?.getString("name").orEmpty()
                RoomPlaylistDetailScreen(
                    playlistName = name,
                    onBack = { nav.popBackStack() }
                )
            }

            composable("library") { LibraryScreen() }
            composable("favorites") { FavoritesScreen() }
            composable("recent") { RecentScreen() }
            composable("artists") { ArtistsScreen() }
            composable("albums") { AlbumsScreen() }

            composable("queue") {
                QueueScreen(onBack = { nav.popBackStack() })
            }

            composable("player") {
                FullPlayerScreen(
                    onBack = { nav.popBackStack() },
                    onOpenQueue = { nav.navigate("queue") }
                )
            }

            composable("playlists") {
                PlaylistsScreen(
                    store = store,
                    onOpen = { playlist -> nav.navigate("playlist/$playlist") },
                    onImport = { nav.navigate("library") }
                )
            }

            composable("search") {
                SearchAndImportScreen(store)
            }

            composable(
                route = "playlist/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { entry ->
                val name = entry.arguments?.getString("name").orEmpty()
                PlaylistDetailScreen(
                    playlistName = name,
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
