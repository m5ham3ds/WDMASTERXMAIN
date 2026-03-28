package com.wdmaster.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wdmaster.app.service.TestService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_PAUSE", "action_pause" -> {
                val serviceIntent = Intent(context, TestService::class.java)
                context.startService(serviceIntent)
            }
            "ACTION_STOP", "action_stop" -> {
                val serviceIntent = Intent(context, TestService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }
}