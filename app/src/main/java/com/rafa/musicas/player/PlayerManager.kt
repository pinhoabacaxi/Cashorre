package com.rafa.musicas.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * Single ExoPlayer instance.
 * "App-only volume" is a player volume multiplier (0..1), not system stream volume.
 */
object PlayerManager {
    private var player: ExoPlayer? = null
    private var appVolume: Float = 1.0f

    fun get(context: Context): ExoPlayer {
        player?.let { return it }
        val p = ExoPlayer.Builder(context).build().apply {
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
        val p = get(context)
        p.setMediaItems(items, startIndex, 0L)
        p.prepare()
        p.playWhenReady = true
    }

    fun setAppVolume(context: Context, v: Float) {
        appVolume = v.coerceIn(0f, 1f)
        get(context).volume = appVolume
    }

    fun getAppVolume(): Float = appVolume
}
