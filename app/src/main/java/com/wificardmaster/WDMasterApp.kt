package com.wdmaster.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wdmaster.app.data.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

@HiltAndroidApp
class WDMasterApp : Application() {

    companion object {
        lateinit var instance: WDMasterApp
            private set

        const val NOTIFICATION_CHANNEL_ID = "wdmaster_foreground"
        const val NOTIFICATION_CHANNEL_NAME = "WDMASTER Service"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Shows test progress and controls"
    }

    @Inject
    lateinit var settingsRepository: com.wdmaster.app.data.repository.SettingsRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        loadDarkModeSetting()
        cleanupOldData()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = NOTIFICATION_CHANNEL_DESCRIPTION
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                // ✅ الإصلاح هنا
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadDarkModeSetting() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = applicationContext.dataStore.data.first()
                val darkMode = prefs[booleanPreferencesKey("dark_mode")] ?: false

                AppCompatDelegate.setDefaultNightMode(
                    if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            } catch (e: Exception) {
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
            }
        }
    }

    private fun cleanupOldData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@WDMasterApp)
                val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

                db.resultDao().deleteOldResults(threshold)
                db.patternDao().cleanupOldPatterns(threshold)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }
}
