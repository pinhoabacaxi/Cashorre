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
        bottomBar = { PlayerMiniBar() }
    ) { padding ->

        NavHost(
            navController = nav,
            startDestination = "playlists",
            modifier = Modifier.padding(padding)
        ) {

            composable("playlists") {
                PlaylistsScreen(
                store = store,
                  onOpen = { playlist ->
                    nav.navigate("playlist/$playlist")
                },
                onImport = {
                    nav.navigate("search")
                }
            )
            }

            composable("search") {
                SearchAndImportScreen(store)
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
}
