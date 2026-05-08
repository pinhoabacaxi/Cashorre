package com.rafa.musicas.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONArray
import org.json.JSONObject

object PlayerManager {

    private const val PREFS = "cashorre_player"
    private const val KEY_QUEUE = "queue"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position"
    private const val KEY_REPEAT = "repeat"
    private const val KEY_SHUFFLE = "shuffle"
    private const val KEY_VOLUME = "volume"

    private var player: ExoPlayer? = null

    fun get(context: Context): ExoPlayer {

        if (player == null) {

            player =
                ExoPlayer.Builder(context.applicationContext)
                    .build()
                    .apply {

                        val audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(C.USAGE_MEDIA)
                                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                                .build()

                        setAudioAttributes(audioAttributes, true)

                        setHandleAudioBecomingNoisy(true)

                        playWhenReady = false

                        repeatMode = Player.REPEAT_MODE_OFF

                        shuffleModeEnabled = false

                        playbackParameters =
                            PlaybackParameters(1f)

                        addListener(
                            object : Player.Listener {

                                override fun onIsPlayingChanged(
                                    isPlaying: Boolean
                                ) {
                                    saveQueue(context)
                                }

                                override fun onMediaItemTransition(
                                    mediaItem: MediaItem?,
                                    reason: Int
                                ) {
                                    saveQueue(context)
                                }

                                override fun onPlaybackStateChanged(
                                    playbackState: Int
                                ) {
                                    saveQueue(context)
                                }
                            }
                        )
                    }

            restoreQueue(context)
        }

        return player!!
    }

    private fun ensurePlaybackService(
        context: Context
    ) {

        val appContext =
            context.applicationContext

        ContextCompat.startForegroundService(
            appContext,
            Intent(
                appContext,
                PlaybackService::class.java
            )
        )
    }

    fun setQueueAndPlay(
        context: Context,
        items: List<MediaItem>,
        startIndex: Int = 0
    ) {

        if (items.isEmpty()) return

        ensurePlaybackService(context)

        val safeIndex =
            startIndex.coerceIn(items.indices)

        val p = get(context)

        p.setMediaItems(
            items,
            safeIndex,
            0L
        )

        p.prepare()

        p.playWhenReady = true

        saveQueue(context)
    }

    fun playNow(
        context: Context,
        item: MediaItem
    ) {

        ensurePlaybackService(context)

        val p = get(context)

        p.setMediaItem(item)

        p.prepare()

        p.play()

        saveQueue(context)
    }

    fun playNext(
        context: Context,
        item: MediaItem
    ) {

        ensurePlaybackService(context)

        val p = get(context)

        val nextIndex =
            (p.currentMediaItemIndex + 1)
                .coerceAtLeast(0)

        p.addMediaItem(nextIndex, item)

        saveQueue(context)
    }

    fun addToQueueEnd(
        context: Context,
        item: MediaItem
    ) {

        ensurePlaybackService(context)

        val p = get(context)

        p.addMediaItem(item)

        saveQueue(context)
    }

    fun resume(
        context: Context
    ) {

        ensurePlaybackService(context)

        val p = get(context)

        p.play()

        saveQueue(context)
    }

    fun pause(
        context: Context
    ) {

        get(context).pause()

        saveQueue(context)
    }

    fun stop(
        context: Context
    ) {

        val p = get(context)

        p.stop()

        saveQueue(context)
    }

    fun release() {

        player?.release()

        player = null
    }

    fun saveQueue(
        context: Context
    ) {

        val p = player ?: return

        val prefs =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

        val array = JSONArray()

        p.mediaItems.forEach { mediaItem ->

            val uri =
                mediaItem.localConfiguration?.uri
                    ?.toString()
                    ?: return@forEach

            val metadata =
                mediaItem.mediaMetadata

            val json =
                JSONObject().apply {

                    put("uri", uri)

                    put(
                        "title",
                        metadata.title?.toString()
                    )

                    put(
                        "artist",
                        metadata.artist?.toString()
                    )

                    put(
                        "artwork",
                        metadata.artworkUri?.toString()
                    )
                }

            array.put(json)
        }

        prefs.edit()
            .putString(KEY_QUEUE, array.toString())
            .putInt(
                KEY_INDEX,
                p.currentMediaItemIndex
            )
            .putLong(
                KEY_POSITION,
                p.currentPosition
            )
            .putInt(
                KEY_REPEAT,
                p.repeatMode
            )
            .putBoolean(
                KEY_SHUFFLE,
                p.shuffleModeEnabled
            )
            .putFloat(
                KEY_VOLUME,
                p.volume
            )
            .apply()
    }

    private fun restoreQueue(
        context: Context
    ) {

        val prefs =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

        val queueString =
            prefs.getString(KEY_QUEUE, null)
                ?: return

        val p = player ?: return

        try {

            val array =
                JSONArray(queueString)

            val items =
                mutableListOf<MediaItem>()

            for (i in 0 until array.length()) {

                val json =
                    array.getJSONObject(i)

                val uri =
                    json.optString("uri")

                if (uri.isBlank()) continue

                val mediaItem =
                    MediaItem.Builder()
                        .setUri(Uri.parse(uri))
                        .setMediaId(uri)
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(
                                    json.optString("title")
                                )
                                .setArtist(
                                    json.optString("artist")
                                )
                                .setArtworkUri(
                                    json.optString("artwork")
                                        .takeIf {
                                            it.isNotBlank()
                                        }?.let {
                                            Uri.parse(it)
                                        }
                                )
                                .build()
                        )
                        .build()

                items.add(mediaItem)
            }

            if (items.isEmpty()) return

            val index =
                prefs.getInt(KEY_INDEX, 0)
                    .coerceIn(items.indices)

            val position =
                prefs.getLong(KEY_POSITION, 0L)

            p.setMediaItems(
                items,
                index,
                position
            )

            p.repeatMode =
                prefs.getInt(
                    KEY_REPEAT,
                    Player.REPEAT_MODE_OFF
                )

            p.shuffleModeEnabled =
                prefs.getBoolean(
                    KEY_SHUFFLE,
                    false
                )

            p.volume =
                prefs.getFloat(
                    KEY_VOLUME,
                    1f
                )

            p.prepare()

        } catch (_: Exception) {
        }
    }

    fun setAppVolume(
        context: Context,
        value: Float
    ) {

        val safeValue =
            value.coerceIn(0f, 1f)

        get(context).volume = safeValue

        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .putFloat(KEY_VOLUME, safeValue)
            .apply()
    }

    fun getAppVolume(): Float {
        return player?.volume ?: 1f
    }
}
