package com.wdmaster.app.service

import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.data.model.TestSettings
import com.wdmaster.app.data.model.TestStats

sealed class ServiceState {
    object Idle : ServiceState()
    object Starting : ServiceState()
    object Running : ServiceState()
    object Paused : ServiceState()
    object Stopping : ServiceState()
    object Stopped : ServiceState()
    data class Error(val message: String, val exception: Throwable? = null) : ServiceState()
    
    fun isRunning(): Boolean = this is Running || this is Paused
    fun isPaused(): Boolean = this is Paused
    fun canStart(): Boolean = this is Idle || this is Stopped
    fun canPause(): Boolean = this is Running
    fun canResume(): Boolean = this is Paused
    fun canStop(): Boolean = isRunning()
}

data class ServiceContext(
    val state: ServiceState = ServiceState.Idle,
    val config: CardConfig = CardConfig(),
    val settings: TestSettings = TestSettings.DEFAULT,
    val stats: TestStats = TestStats(),
    val currentCard: String = "",
    val startTime: Long = 0,
    val pauseTime: Long = 0,
    val totalPausedDuration: Long = 0
) {
    fun withState(newState: ServiceState): ServiceContext {
        return copy(state = newState)
    }
    
    fun withStats(newStats: TestStats): ServiceContext {
        return copy(stats = newStats)
    }
    
    fun withConfig(newConfig: CardConfig): ServiceContext {
        return copy(config = newConfig)
    }
    
    fun withSettings(newSettings: TestSettings): ServiceContext {
        return copy(settings = newSettings)
    }
    
    fun withCurrentCard(card: String): ServiceContext {
        return copy(currentCard = card)
    }
}