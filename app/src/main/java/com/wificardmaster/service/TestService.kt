package com.wdmaster.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.wdmaster.app.WDMasterApp
import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.data.model.TestSettings
import com.wdmaster.app.data.model.TestStats
import com.wdmaster.app.data.repository.PatternRepository
import com.wdmaster.app.data.repository.ResultRepository
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.domain.learning.PatternLearningSystem
import com.wdmaster.app.notification.WDMasterNotification
import com.wdmaster.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class TestService : Service(), TestServiceBridge {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var routerRepository: RouterRepository
    @Inject lateinit var patternRepository: PatternRepository
    @Inject lateinit var resultRepository: ResultRepository
    @Inject lateinit var patternLearningSystem: PatternLearningSystem

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _events = MutableSharedFlow<ServiceEvent>(replay = 1)

    private var isRunning = false
    private var isPaused = false
    private var currentConfig = CardConfig()
    private var currentSettings = TestSettings()
    private var currentStats = TestStats()
    private val testedCards = mutableSetOf<String>()
    private var wakeLock: PowerManager.WakeLock? = null
    private var notificationManager: WDMasterNotification? = null
    private var currentRouterId: Int? = null

    inner class LocalBinder : Binder() {
        fun getService(): TestService = this@TestService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notificationManager = WDMasterNotification(this)

        serviceScope.launch {
            settingsRepository.settingsFlow.collect {
                currentSettings = it
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(2001, buildNotification("Ready", 0))
        return START_STICKY
    }

    override fun observeEvents(): Flow<ServiceEvent> = _events.asSharedFlow()

    override fun sendCommand(command: TestServiceBridge.ServiceCommand) {
        when (command) {
            is TestServiceBridge.ServiceCommand.Start -> startTesting()
            is TestServiceBridge.ServiceCommand.Pause -> togglePause()
            is TestServiceBridge.ServiceCommand.Resume -> togglePause()
            is TestServiceBridge.ServiceCommand.Stop -> stopTesting()

            is TestServiceBridge.ServiceCommand.UpdateConfig -> {
                currentConfig = command.config
                _events.tryEmit(ServiceEvent.ConfigUpdate(command.config))
            }

            is TestServiceBridge.ServiceCommand.UpdateSettings -> {
                currentSettings = currentSettings.copy(
                    skipTested = command.skipTested,
                    stopOnSuccess = command.stopOnSuccess,
                    delayMs = command.delayMs
                )
            }
        }
    }

    override fun isRunning() = isRunning
    override fun isPaused() = isPaused
    override fun getCurrentStats() = currentStats

    private fun startTesting() {
        if (isRunning) return

        isRunning = true
        isPaused = false

        serviceScope.launch {
            _events.emit(ServiceEvent.ServiceStarted)
            acquireWakeLock()
            runTestLoop()
        }
    }

    private suspend fun runTestLoop() {
        var tries = 0L
        val startTime = System.currentTimeMillis()

        currentStats = currentStats.copy(startTime = startTime, isRunning = true)

        while (isRunning && tries < currentConfig.maxTries) {

            if (isPaused) {
                delay(100)
                continue
            }

            val card = generateRandomCard()
            _events.emit(ServiceEvent.CardGenerated(card))

            val testStartTime = System.currentTimeMillis()
            val result = performTest(card)
            val responseTime = System.currentTimeMillis() - testStartTime

            handleTestResult(card, result, responseTime)

            tries++
            updateStats(tries, startTime, card)

            if (result && currentSettings.stopOnSuccess) {
                stopTesting()
                break
            }

            delay(currentSettings.delayMs)
        }
    }

    private fun generateRandomCard(): String {
        val sb = StringBuilder(currentConfig.prefix)

        while (sb.length < currentConfig.length) {
            val i = (Math.random() * currentConfig.allowedChars.length).toInt()
            sb.append(currentConfig.allowedChars[i])
        }

        return sb.toString()
    }

    private suspend fun performTest(card: String): Boolean {
        delay(100)
        return Math.random() > 0.95
    }

    private suspend fun handleTestResult(card: String, success: Boolean, time: Long) {
        _events.emit(ServiceEvent.TestResult(card, success, time))
    }

    private suspend fun updateStats(tries: Long, start: Long, card: String) {
        val elapsed = max(1, (System.currentTimeMillis() - start) / 1000)

        currentStats = currentStats.copy(
            tested = tries,
            speed = tries.toDouble() / elapsed,
            currentCard = card
        )

        _events.emit(ServiceEvent.StatsUpdate(currentStats))
    }

    private fun togglePause() {
        isPaused = !isPaused

        serviceScope.launch {
            if (isPaused) {
                _events.emit(ServiceEvent.ServicePaused)
            } else {
                _events.emit(ServiceEvent.ServiceResumed)
            }
        }
    }

    private fun stopTesting() {
        isRunning = false
        isPaused = false
        releaseWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String, progress: Int) =
        NotificationCompat.Builder(this, WDMasterApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(com.wdmaster.app.R.drawable.ic_wifi)
            .setContentTitle("WDMASTER")
            .setContentText(text)
            .setProgress(100, progress, false)
            .build()

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wdmaster").apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        releaseWakeLock()
    }
}
