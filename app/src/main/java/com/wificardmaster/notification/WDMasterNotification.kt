package com.wdmaster.app.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wdmaster.app.R
import com.wdmaster.app.WDMasterApp
import com.wdmaster.app.data.model.TestStats
import com.wdmaster.app.ui.MainActivity

class WDMasterNotification(private val context: Context) {
    
    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val ACTION_PAUSE = "action_pause"
        private const val ACTION_STOP = "action_stop"
    }
    
    private val notificationManager: NotificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createChannel()
    }
    
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WDMasterApp.NOTIFICATION_CHANNEL_ID,
                WDMasterApp.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows test progress and controls"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(
        currentCard: String?,
        stats: TestStats,
        isRunning: Boolean,        isPaused: Boolean
    ): Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val pauseIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_PAUSE
            putExtra("is_paused", !isPaused)
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            context, 1, pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(context, WDMasterApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wifi)
            .setContentTitle("WiFi Card Master Pro")
            .setContentText(currentCard ?: "Initializing...")
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, (stats.progress * 100).toInt(), false)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(buildDetailedText(stats, currentCard)))
            .addAction(
                if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                if (isPaused) "Resume" else "Pause",
                pausePendingIntent
            )
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .build()
    }
    
    fun buildNotification(currentCard: String?, progress: Int): Notification {
        return NotificationCompat.Builder(context, WDMasterApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wifi)            .setContentTitle("WiFi Card Master Pro")
            .setContentText(currentCard ?: "Initializing...")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }
    
    private fun buildDetailedText(stats: TestStats, card: String?): String {
        return """
            Current: ${card?.takeLast(8) ?: "N/A"}
            ━━━━━━━━━━━━━━━━
            Tested: ${stats.tested}
            ✓ Success: ${stats.success}
            ✗ Failed: ${stats.failure}
            ━━━━━━━━━━━━━━━━
            Speed: ${"%.2f".format(stats.speed)}/sec
            ETA: ${formatETA(stats.etaSeconds)}
            Success Rate: ${stats.getSuccessRate()}%
        """.trimIndent()
    }
    
    private fun formatETA(seconds: Long): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
    
    fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}