package com.rafa.musicas

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.rafa.musicas.data.PlaylistStore
import com.rafa.musicas.ui.AppRoot

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                
                // Inicializa o store e passa para o AppRoot
                val store = remember { PlaylistStore(this) }
                AppRoot(store = store)
            }
        }
    }
}
