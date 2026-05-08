package com.rafa.musicas.player

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.rafa.musicas.MainActivity

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var contentIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val player = PlayerManager.get(this)

        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        contentIntent = pendingIntent

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        try {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(player, pendingIntent)
            )
        } catch (_: SecurityException) {
            stopSelf()
            return
        }

        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    PlayerManager.saveQueue(this@PlaybackService)
                    updateNotification(player)
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int
                ) {
                    PlayerManager.saveQueue(this@PlaybackService)
                    updateNotification(player)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    PlayerManager.saveQueue(this@PlaybackService)
                    updateNotification(player)
                }
            }
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val result = super.onStartCommand(intent, flags, startId)

        val player = PlayerManager.get(this)

        when (intent?.action) {
            ACTION_PREVIOUS -> {
                player.seekToPrevious()
                player.play()
            }

            ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }

            ACTION_NEXT -> {
                player.seekToNext()
                player.play()
            }

            ACTION_STOP -> {
                player.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        PlayerManager.saveQueue(this)
        updateNotification(player)

        return result
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player

        if (player == null || player.playbackState == Player.STATE_IDLE) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun updateNotification(player: Player) {
        val pendingIntent = contentIntent ?: return

        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(
            NOTIFICATION_ID,
            buildNotification(player, pendingIntent)
        )
    }

    private fun buildNotification(
        player: Player,
        pendingIntent: PendingIntent
    ): Notification {
        val metadata = player.currentMediaItem?.mediaMetadata

        val title = metadata?.displayTitle?.toString()
            ?: metadata?.title?.toString()
            ?: "Cashorre"

        val artist = metadata?.artist?.toString()
            ?: "Reproduzindo música"

        val playPauseIcon =
            if (player.isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }

        val playPauseText =
            if (player.isPlaying) {
                "Pausar"
            } else {
                "Tocar"
            }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(pendingIntent)
            .setOngoing(player.isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_previous,
                "Anterior",
                servicePendingIntent(ACTION_PREVIOUS, REQUEST_PREVIOUS)
            )
            .addAction(
                playPauseIcon,
                playPauseText,
                servicePendingIntent(ACTION_PLAY_PAUSE, REQUEST_PLAY_PAUSE)
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Próxima",
                servicePendingIntent(ACTION_NEXT, REQUEST_NEXT)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Parar",
                servicePendingIntent(ACTION_STOP, REQUEST_STOP)
            )
            .build()
    }

    private fun servicePendingIntent(
        action: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).apply {
            this.action = action
        }

        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reprodução",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "cashorre_playback"
        private const val NOTIFICATION_ID = 1001

        private const val ACTION_PREVIOUS = "com.rafa.musicas.action.PREVIOUS"
        private const val ACTION_PLAY_PAUSE = "com.rafa.musicas.action.PLAY_PAUSE"
        private const val ACTION_NEXT = "com.rafa.musicas.action.NEXT"
        private const val ACTION_STOP = "com.rafa.musicas.action.STOP"

        private const val REQUEST_OPEN_APP = 10
        private const val REQUEST_PREVIOUS = 11
        private const val REQUEST_PLAY_PAUSE = 12
        private const val REQUEST_NEXT = 13
        private const val REQUEST_STOP = 14
    }
}
