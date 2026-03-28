package com.wdmaster.app.service

import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.data.model.TestStats
import kotlinx.coroutines.flow.Flow

interface TestServiceBridge {
    
    sealed class ServiceEvent {
        data class LogMessage(val msg: String, val type: LogType) : ServiceEvent()
        data class StatsUpdate(val stats: TestStats) : ServiceEvent()
        data class CardGenerated(val card: String) : ServiceEvent()
        data class TestResult(val card: String, val success: Boolean, val responseTime: Long) : ServiceEvent()
        data class ConfigUpdate(val config: CardConfig) : ServiceEvent()
        object ServiceStarted : ServiceEvent()
        object ServicePaused : ServiceEvent()
        object ServiceResumed : ServiceEvent()
        object ServiceStopped : ServiceEvent()
        object ServiceError : ServiceEvent()
    }
    
    enum class LogType {
        INFO,
        SUCCESS,
        ERROR,
        WARNING,
        DEBUG
    }
    
    sealed class ServiceCommand {
        object Start : ServiceCommand()
        object Pause : ServiceCommand()
        object Resume : ServiceCommand()
        object Stop : ServiceCommand()
        data class UpdateConfig(val config: CardConfig) : ServiceCommand()
        data class UpdateSettings(val skipTested: Boolean, val stopOnSuccess: Boolean, val delayMs: Long) : ServiceCommand()
    }
    
    fun observeEvents(): Flow<ServiceEvent>
    fun sendCommand(command: ServiceCommand)
    fun isRunning(): Boolean
    fun isPaused(): Boolean
    fun getCurrentStats(): TestStats
}