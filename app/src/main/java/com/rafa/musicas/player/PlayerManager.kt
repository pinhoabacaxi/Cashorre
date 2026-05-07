package com.rafa.musicas.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object PlayerManager {
    private var player: ExoPlayer? = null
    private var appVolume: Float = 1.0f
    private var lastQueue: List<MediaItem> = emptyList()
    private var lastIndex: Int = 0

    fun get(context: Context): ExoPlayer {
        player?.let { return it }

        val appContext = context.applicationContext

        val p = ExoPlayer.Builder(appContext).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            volume = appVolume
        }

        player = p
        return p
    }

    fun setQueueAndPlay(context: Context, items: List<MediaItem>, startIndex: Int = 0) {
        if (items.isEmpty()) return

        lastQueue = items
        lastIndex = startIndex.coerceIn(items.indices)

        val p = get(context)
        p.setMediaItems(items, lastIndex, 0L)
        p.prepare()
        p.playWhenReady = true
    }

    fun playOrPause(context: Context) {
        val p = get(context)
        if (p.isPlaying) p.pause() else p.play()
    }

    fun setAppVolume(context: Context, value: Float) {
        appVolume = value.coerceIn(0f, 1f)
        get(context).volume = appVolume
    }

    fun getAppVolume(): Float = appVolume
}
