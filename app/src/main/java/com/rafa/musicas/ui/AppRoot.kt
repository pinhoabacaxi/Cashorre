package com.rafa.musicas.ui

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.PlaybackService

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(store: PlaylistStore) {

    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, PlaybackService::class.java)
        )
    }

   Scaffold(
    bottomBar = {
        PlayerMiniBar(
            onOpenPlayer = {
                nav.navigate("player")
            }
        )
    }
) 

        NavHost(
            navController = nav,
            startDestination = "room_playlists"
            modifier = Modifier.padding(padding)
        ) {

        
            composable("player") {
                FullPlayerScreen(
                onBack = { nav.popBackStack() }
                )
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
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { entry ->
                 val name = entry.arguments?.getString("name").orEmpty()
                 RoomPlaylistDetailScreen(
                    playlistName = name,
                    onBack = { nav.popBackStack() }
                 )
            }
            composable("artists") {
                ArtistsScreen()
            }

            composable("albums") {
                AlbumsScreen()
            }
            composable("library") {
                LibraryScreen()
            }
            
            composable("favorites") {
                FavoritesScreen()
            }

            composable("recent") {
                RecentScreen()
            }
            
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
            Button(onClick = { nav.navigate("library") }) { Text("Biblioteca") }
            Button(onClick = { nav.navigate("favorites") }) { Text("Favoritos") }
            Button(onClick = { nav.navigate("recent") }) { Text("Recentes") }
            
            composable("queue") {
                QueueScreen(
                    onBack = { nav.popBackStack() }
                 )
            }
            composable(
                route = "playlist/{name}",
                arguments = listOf(navArgument("name") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->

                val name = backStackEntry.arguments?.getString("name") ?: ""

                PlaylistDetailScreen(
                    playlistName = name,
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
