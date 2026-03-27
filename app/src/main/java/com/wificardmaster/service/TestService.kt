package com.wdmaster.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
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
import java.util.*
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class TestService : Service(), TestServiceBridge {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    @Inject
    lateinit var routerRepository: RouterRepository
    
    @Inject
    lateinit var patternRepository: PatternRepository
    
    @Inject
    lateinit var resultRepository: ResultRepository
    
    @Inject
    lateinit var patternLearningSystem: PatternLearningSystem
    
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
    
    private val job = MutableStateFlow<Job?>(null)
    
    inner class LocalBinder : Binder() {
        fun getService(): TestService = this@TestService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = WDMasterNotification(this)
        
        serviceScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                currentSettings = settings
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
                    stopOnSuccess = command.stopOnSuccess,                    delayMs = command.delayMs
                )
            }
        }
    }
    
    override fun isRunning(): Boolean = isRunning
    
    override fun isPaused(): Boolean = isPaused
    
    override fun getCurrentStats(): TestStats = currentStats
    
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
        
        currentStats = currentStats.copy(
            startTime = startTime,
            isRunning = true,
            isPaused = false
        )
        
        while (isRunning && tries < currentConfig.maxTries) {
            if (isPaused) {
                delay(100)
                continue
            }
            
            val card = generateCard()
            _events.emit(ServiceEvent.CardGenerated(card))
            
            if (currentSettings.skipTested && card in testedCards) {
                _events.emit(ServiceEvent.LogMessage("⚠ Skipping duplicate: ${card.takeLast(8)}", ServiceEvent.LogType.WARNING))
                continue
            }
            
            val testStartTime = System.currentTimeMillis()
            val result = performTest(card)            val responseTime = System.currentTimeMillis() - testStartTime
            
            testedCards.add(card)
            handleTestResult(card, result, responseTime)
            
            tries++
            updateStats(tries, startTime, card)
            
            if (result && currentSettings.stopOnSuccess) {
                _events.emit(ServiceEvent.LogMessage("✅ Success found! Stopping.", ServiceEvent.LogType.SUCCESS))
                stopTesting()
                break
            }
            
            delay(currentSettings.delayMs)
        }
        
        if (tries >= currentConfig.maxTries) {
            _events.emit(ServiceEvent.LogMessage("📊 Max tries reached.", ServiceEvent.LogType.INFO))
            stopTesting()
        }
    }
    
    private fun generateCard(): String {
        return if (currentConfig.useLearnedPatterns && currentRouterId != null) {
            runBlocking {
                val patterns = patternRepository.getPatternsForRouter(currentRouterId).first()
                if (patterns.isNotEmpty()) {
                    val bestPattern = patterns.maxByOrNull { it.getWeight() }
                    bestPattern?.toCard(currentConfig.allowedChars) ?: generateRandomCard()
                } else {
                    generateRandomCard()
                }
            }
        } else {
            generateRandomCard()
        }
    }
    
    private fun generateRandomCard(): String {
        val sb = StringBuilder(currentConfig.prefix)
        while (sb.length < currentConfig.length) {
            val randomIndex = (Math.random() * currentConfig.allowedChars.length).toInt()
            sb.append(currentConfig.allowedChars[randomIndex])
        }
        return sb.toString()
    }
    
    private suspend fun performTest(card: String): Boolean {
        delay(50 + (Math.random() * 100).toLong())        return Math.random() > 0.95
    }
    
    private suspend fun handleTestResult(card: String, success: Boolean, responseTime: Long) {
        val type = if (success) ServiceEvent.LogType.SUCCESS else ServiceEvent.LogType.ERROR
        val msg = if (success) "✅ $card" else "❌ $card"
        _events.emit(ServiceEvent.LogMessage(msg, type))
        _events.emit(ServiceEvent.TestResult(card, success, responseTime))
        
        val testResult = com.wdmaster.app.data.model.TestResult(
            card = card,
            success = success,
            routerId = currentRouterId,
            responseTime = responseTime
        )
        resultRepository.addResult(testResult)
        
        if (success) {
            currentStats = currentStats.copy(success = currentStats.success + 1)
            patternLearningSystem.recordSuccess(card, currentRouterId)
        } else {
            currentStats = currentStats.copy(failure = currentStats.failure + 1)
        }
    }
    
    private suspend fun updateStats(tries: Long, startTime: Long, card: String) {
        val elapsedSec = max(1, (System.currentTimeMillis() - startTime) / 1000)
        val speed = tries.toDouble() / elapsedSec
        val remaining = currentConfig.maxTries - tries
        val eta = if (speed > 0) (remaining / speed).toLong() else 0
        
        currentStats = currentStats.copy(
            tested = tries,
            progress = tries.toFloat() / currentConfig.maxTries,
            speed = speed,
            etaSeconds = eta,
            currentCard = card
        )
        
        _events.emit(ServiceEvent.StatsUpdate(currentStats))
        
        notificationManager?.updateNotification(
            buildNotification(card, (currentStats.progress * 100).toInt())
        )
    }
    
    private fun togglePause() {
        isPaused = !isPaused
        serviceScope.launch {
            if (isPaused) {                _events.emit(ServiceEvent.ServicePaused)
            } else {
                _events.emit(ServiceEvent.ServiceResumed)
            }
        }
    }
    
    private fun stopTesting() {
        isRunning = false
        isPaused = false
        releaseWakeLock()
        
        serviceScope.launch {
            _events.emit(ServiceEvent.ServiceStopped)
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun buildNotification(currentCard: String?, progress: Int): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val pauseIntent = Intent(this, com.wdmaster.app.notification.NotificationReceiver::class.java).apply {
            action = "ACTION_PAUSE"
            putExtra("is_paused", isPaused)
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            this, 1, pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, com.wdmaster.app.notification.NotificationReceiver::class.java).apply {
            action = "ACTION_STOP"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, WDMasterApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(com.wdmaster.app.R.drawable.ic_wifi)
            .setContentTitle("WiFi Card Master Pro")
            .setContentText(currentCard ?: "Initializing...")            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, false)
            .addAction(com.wdmaster.app.R.drawable.ic_pause, "Pause", pausePendingIntent)
            .addAction(com.wdmaster.app.R.drawable.ic_stop, "Stop", stopPendingIntent)
            .build()
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WDMASTER::TestServiceLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L)
            }
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        releaseWakeLock()
    }
}