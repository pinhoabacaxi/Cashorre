object NotificationHelper {

    fun createNotification(context: Context): Notification {
        val channelId = "player"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Player",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Player ativo")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}
