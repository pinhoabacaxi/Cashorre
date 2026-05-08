package com.rafa.musicas.player

import android.content.Context
import android.media.AudioManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONArray
import org.json.JSONObject
import android.content.Intent
import androidx.core.content.ContextCompat

object PlayerManager {
    private const val PREFS_NAME = "player_state"
    private const val KEY_QUEUE = "queue"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position"
    private const val KEY_VOLUME = "volume"

    private var player: ExoPlayer? = null
    private var appVolume: Float = 1.0f

    fun get(context: Context): ExoPlayer {
        player?.let { return it }

        val appContext = context.applicationContext
        appVolume = loadVolume(appContext)

        val p = ExoPlayer.Builder(appContext).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            volume = appVolume
            setHandleAudioBecomingNoisy(true)
        }

        player = p
        restoreQueue(appContext)
        return p
    }

    fun setQueueAndPlay(
        context: Context,
        items: List<MediaItem>,
        startIndex: Int = 0
    ) {
        if (items.isEmpty()) return 
        
        ensurePlaybackService(context)

        val safeIndex = startIndex.coerceIn(items.indices)
        val p = get(context)

        p.setMediaItems(items, safeIndex, 0L)
        p.prepare()
        p.playWhenReady = true
        saveQueue(context)
    }

    fun playNow(context: Context, item: MediaItem) {
        ensurePlaybackService(context)
        val p = get(context)
        p.setMediaItem(item)
        p.prepare()
        p.playWhenReady = true
        saveQueue(context)
    }

    fun playNext(context: Context, item: MediaItem) {
        ensurePlaybackService(context)
        val p = get(context)
        val nextIndex = (p.currentMediaItemIndex + 1).coerceAtLeast(0)

        if (p.mediaItemCount == 0) {
            playNow(context, item)
        } else {
            p.addMediaItem(nextIndex, item)
            saveQueue(context)
        }
    }

    fun addToQueueEnd(context: Context, item: MediaItem) {
        ensurePlaybackService(context)
        val p = get(context)

        if (p.mediaItemCount == 0) {
            playNow(context, item)
        } else {
            p.addMediaItem(item)
            saveQueue(context)
        }
    }

    fun removeFromQueue(context: Context, index: Int) {
        val p = get(context)

        if (index in 0 until p.mediaItemCount) {
            p.removeMediaItem(index)
            saveQueue(context)
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
            saveQueue(context)
        }
    }

    fun getQueue(context: Context): List<MediaItem> {
        val p = get(context)

        return List(p.mediaItemCount) { index ->
            p.getMediaItemAt(index)
        }
    }

    fun saveQueue(context: Context) {
        val p = get(context)
        val array = JSONArray()

        for (i in 0 until p.mediaItemCount) {
            val item = p.getMediaItemAt(i)
            val metadata = item.mediaMetadata

            val obj = JSONObject()
                .put("mediaId", item.mediaId)
                .put("uri", item.localConfiguration?.uri?.toString())
                .put("title", metadata.displayTitle?.toString())
                .put("artist", metadata.artist?.toString())
                .put("album", metadata.albumTitle?.toString())
                .put("artworkUri", metadata.artworkUri?.toString())

            array.put(obj)
        }

        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_QUEUE, array.toString())
            .putInt(KEY_INDEX, p.currentMediaItemIndex)
            .putLong(KEY_POSITION, p.currentPosition.coerceAtLeast(0L))
            .putFloat(KEY_VOLUME, appVolume)
            .apply()
    }
    private fun ensurePlaybackService(context: Context) {
        val appContext = context.applicationContext

        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, PlaybackService::class.java)
        )
    }
    private fun ensurePlaybackService(context: Context) {
        val appContext = context.applicationContext

        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, PlaybackService::class.java)
        )
    }
    private fun restoreQueue(context: Context) {
        val prefs =
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val raw = prefs.getString(KEY_QUEUE, null) ?: return

        val items = runCatching {
            val array = JSONArray(raw)

            List(array.length()) { index ->
                val obj = array.getJSONObject(index)

                val metadataBuilder = MediaMetadata.Builder()
                    .setDisplayTitle(obj.optString("title", "Sem título"))
                    .setArtist(obj.optString("artist", "Desconhecido"))
                    .setAlbumTitle(obj.optString("album", ""))

                val artwork = obj.optString("artworkUri", "")

                if (artwork.isNotBlank()) {
                    metadataBuilder.setArtworkUri(
                        android.net.Uri.parse(artwork)
                    )
                }

                MediaItem.Builder()
                    .setMediaId(obj.optString("mediaId"))
                    .setUri(obj.optString("uri"))
                    .setMediaMetadata(metadataBuilder.build())
                    .build()
            }
        }.getOrDefault(emptyList())

        if (items.isEmpty()) return

        val index =
            prefs.getInt(KEY_INDEX, 0)
                .coerceIn(items.indices)

        val position =
            prefs.getLong(KEY_POSITION, 0L)
                .coerceAtLeast(0L)

        val p = get(context)
        p.setMediaItems(items, index, position)
        p.prepare()
    }

    fun setAppVolume(context: Context, value: Float) {
        appVolume = value.coerceIn(0f, 1f)

        get(context).volume = appVolume

        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_VOLUME, appVolume)
            .apply()
    }

    fun getAppVolume(): Float = appVolume

    private fun loadVolume(context: Context): Float {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_VOLUME, 1.0f)
            .coerceIn(0f, 1f)
    }

    fun pause(context: Context) {
        get(context).pause()
        saveQueue(context)
    }

    fun resume(context: Context) {
        get(context).play()
        saveQueue(context)
    }

    fun stop(context: Context) {
        val p = get(context)
        p.stop()
        saveQueue(context)
    }
}
