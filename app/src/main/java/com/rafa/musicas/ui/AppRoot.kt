package com.rafa.musicas.ui

import android.content.Intent
import androidx.annotation.OptIn // ADICIONADO
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi // ADICIONADO
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.player.PlaybackService

// ... (Routes permanecem iguais)

@OptIn(UnstableApi::class) // ESTA LINHA CORRIGE O ERRO DO LINT
@ExperimentalMaterial3Api
@Composable
fun AppRoot( store ) {
    val nav = rememberNavController()
    val context = LocalContext.current

    // O Lint falhava aqui porque o PlaybackService (Media3) é considerado UnstableApi
    LaunchedEffect(Unit) {
        context.startService(Intent(context, PlaybackService::class.java))
    }

    val store = remember {PlaylistStore(context)}}
    // ... resto do seu código (Drawer, Scaffold, NavHost)
    
