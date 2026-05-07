package com.rafa.musicas.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object PlayerManager {
    private var player: ExoPlayer? = null
    private var appVolume: Float = 1.0f

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

    fun setQueueAndPlay(
        context: Context,
        items: List<MediaItem>,
        startIndex: Int = 0
    ) {
        if (items.isEmpty()) return

        val safeIndex = startIndex.coerceIn(items.indices)
        val p = get(context)

        p.setMediaItems(items, safeIndex, 0L)
        p.prepare()
        p.playWhenReady = true
    }

    fun playNow(context: Context, item: MediaItem) {
        val p = get(context)
        p.setMediaItem(item)
        p.prepare()
        p.playWhenReady = true
    }

    fun playNext(context: Context, item: MediaItem) {
        val p = get(context)
        val nextIndex = (p.currentMediaItemIndex + 1).coerceAtLeast(0)

        if (p.mediaItemCount == 0) {
            playNow(context, item)
        } else {
            p.addMediaItem(nextIndex, item)
        }
    }

    fun addToQueueEnd(context: Context, item: MediaItem) {
        val p = get(context)

        if (p.mediaItemCount == 0) {
            playNow(context, item)
        } else {
            p.addMediaItem(item)
        }
    }

    fun removeFromQueue(context: Context, index: Int) {
        val p = get(context)
        if (index in 0 until p.mediaItemCount) {
            p.removeMediaItem(index)
        }
    }

    fun moveQueueItem(context: Context, fromIndex: Int, toIndex: Int) {
        val p = get(context)
        if (
            fromIndex in 0 until p.mediaItemCount &&
            toIndex in 0 until p.mediaItemCount &&
            fromIndex != toIndex
        ) {
            p.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun getQueue(context: Context): List<MediaItem> {
        val p = get(context)
        return List(p.mediaItemCount) { index ->
            p.getMediaItemAt(index)
        }
    }

    fun setAppVolume(context: Context, value: Float) {
        appVolume = value.coerceIn(0f, 1f)
        get(context).volume = appVolume
    }

    fun getAppVolume(): Float = appVolume
}
