package com.rafa.musicas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val mainDestinations = listOf(
    BottomDestination(
        route = "library",
        label = "Biblioteca",
        icon = Icons.Default.LibraryMusic
    ),
    BottomDestination(
        route = "room_playlists",
        label = "Playlists",
        icon = Icons.Default.PlaylistPlay
    ),
    BottomDestination(
        route = "favorites",
        label = "Favoritos",
        icon = Icons.Default.Favorite
    ),
    BottomDestination(
        route = "artists",
        label = "Artistas",
        icon = Icons.Default.Person
    ),
    BottomDestination(
        route = "albums",
        label = "Álbuns",
        icon = Icons.Default.Album
    ),
    BottomDestination(
        route = "settings",
        label = "Config",
        icon = Icons.Default.Settings
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(store: PlaylistStore) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomNav = mainDestinations.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    mainDestinations.forEach { destination ->
                        val selected =
                            currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    nav.navigate(destination.route) {
                                        popUpTo(nav.graph.startDestinationId) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(destination.label)
                            }
                        )
                    }
                }
            } else {
                PlayerMiniBar(
                    onOpenPlayer = {
                        nav.navigate("player")
                    }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = nav,
            startDestination = "library",
            modifier = Modifier.padding(padding)
        ) {
            composable("library") {
                LibraryScreen()
            }

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
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val name = entry.arguments?.getString("name").orEmpty()

                RoomPlaylistDetailScreen(
                    playlistName = name,
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }

            composable("favorites") {
                FavoritesScreen()
            }

            composable("recent") {
                RecentScreen()
            }

            composable("artists") {
                ArtistsScreen()
            }

            composable("albums") {
                AlbumsScreen()
            }

            composable("settings") {
                LibraryScreen()
            }

            composable("queue") {
                QueueScreen(
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }

            composable("player") {
                FullPlayerScreen(
                    onBack = {
                        nav.popBackStack()
                    },
                    onOpenQueue = {
                        nav.navigate("queue")
                    }
                )
            }

            // Rotas antigas mantidas por segurança temporária.
            composable("playlists") {
                PlaylistsScreen(
                    store = store,
                    onOpen = { playlist ->
                        nav.navigate("playlist/$playlist")
                    },
                    onImport = {
                        nav.navigate("library")
                    }
                )
            }

            composable("search") {
                SearchAndImportScreen(store)
            }

            composable(
                route = "playlist/{name}",
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val name = entry.arguments?.getString("name").orEmpty()

                PlaylistDetailScreen(
                    playlistName = name,
                    store = store,
                    onBack = {
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}
