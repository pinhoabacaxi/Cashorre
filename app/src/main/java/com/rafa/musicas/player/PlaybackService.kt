package com.rafa.musicas.player

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        contentIntent = pendingIntent

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        startForeground(
            NOTIFICATION_ID,
            buildNotification(player, pendingIntent)
        )

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
            .build()
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
    }
}
