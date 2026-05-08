package com.rafa.musicas

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity // Correção do 'app' não encontrado
import androidx.lifecycle.lifecycleScope // Correção do lifecycleScope
import com.maxrave.kotlinyoutubeextractor.* // Importa State, YTExtractor e extensões como getVideoOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Correção: Agora especificamos o tipo explicitamente para evitar erro de inferência
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            iniciarExtracaoYoutube()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verificarPermissoes()
    }

    private fun verificarPermissoes() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions.launch(permissions)
    }

    private fun iniciarExtracaoYoutube() {
        val tv = findViewById<TextView>(R.id.textView)
        // Agora o 'this' será reconhecido como Context corretamente
        val yt = YTExtractor(con = this, CACHING = false, LOGGING = true, retryCount = 3)
        var text = "Resultado:\n"

        lifecycleScope.launch(Dispatchers.IO) {
            val listVideoId = listOf("d40rzwlq8l4", "Q2T8-q9fGSI")
            
            listVideoId.forEach { id ->
                yt.extract(id)
                if (yt.state == State.SUCCESS) {
                    val files = yt.getYTFiles()
                    // Agora as extensões do YtFileExt serão encontradas
                    val audio = files?.getAudioOnly()?.bestQuality()
                    text += "Vídeo $id: ${audio?.url}\n"
                }
            }

            withContext(Dispatchers.Main) {
                tv.text = text
            }
        }
    }
}
