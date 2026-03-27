package com.wdmaster.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wdmaster.app.service.TestService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Optional: Start service on boot if needed
            // val serviceIntent = Intent(context, TestService::class.java)
            // context.startForegroundService(serviceIntent)
        }
    }
}