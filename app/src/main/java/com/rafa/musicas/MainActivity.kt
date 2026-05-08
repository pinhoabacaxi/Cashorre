package com.rafa.musicas

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.maxrave.kotlinyoutubeextractor.State
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import com.maxrave.kotlinyoutubeextractor.bestQuality
import com.maxrave.kotlinyoutubeextractor.getAudioOnly
import com.maxrave.kotlinyoutubeextractor.getVideoOnly
// Certifique-se de que o R aponta para o pacote correto do seu projeto
import com.rafa.musicas.R 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Launcher para solicitação de permissões
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Se as permissões forem aceitas, iniciamos a extração
            iniciarExtracaoYoutube()
        } else {
            Log.e("Permissions", "Algumas permissões foram negadas.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Define o layout XML como a interface da Activity
        setContentView(R.layout.activity_main)

        // Verifica e solicita permissões dependendo da versão do Android
        verificarPermissoes()
    }

    private fun verificarPermissoes() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions.launch(permissions)
    }

    private fun iniciarExtracaoYoutube() {
        // Busca o TextView no layout XML
        val tv = findViewById<TextView>(R.id.textView)
        
        val listVideoId = listOf("d40rzwlq8l4", "Q2T8-q9fGSI", "UzvbmzVDCQ4", "aaMv6SJafPA", "JvOg0TSvdGU")
        val yt = YTExtractor(con = this, CACHING = false, LOGGING = true, retryCount = 3)
        var acumuladorTexto = "Iniciando extração...\n\n"

        // Usamos lifecycleScope para evitar vazamentos de memória
        lifecycleScope.launch(Dispatchers.IO) {
            listVideoId.forEach { videoId ->
                yt.extract(videoId)
                
                if (yt.state == State.SUCCESS) {
                    yt.getYTFiles()?.let { files ->
                        // Exemplo: Pegando áudio (Itag 251 é comum para Opus/WebM)
                        val audioUrl = files[251]?.url ?: "URL não encontrada"
                        
                        acumuladorTexto += "ID: $videoId\nLink: $audioUrl\n\n"

                        // Logs de depuração
                        Log.d("YoutubeExtractor", "Extraído: $videoId")
                        Log.d("YoutubeExtractor", "Melhor Áudio: ${files.getAudioOnly()?.bestQuality()}")
                    }
                } else {
                    acumuladorTexto += "Erro ao extrair ID: $videoId\n\n"
                }

                // Atualiza o TextView na Main Thread a cada vídeo processado
                withContext(Dispatchers.Main) {
                    tv.text = acumuladorTexto
                }
            }
        }
    }
}
