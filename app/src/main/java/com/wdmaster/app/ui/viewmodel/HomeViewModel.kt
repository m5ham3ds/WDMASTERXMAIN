package com.wdmaster.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.data.model.TestStats
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.domain.usecase.GenerateCardUseCase
import com.wdmaster.app.service.TestServiceBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val settingsRepository: SettingsRepository,
    private val generateCardUseCase: GenerateCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow(TestStats())
    val stats: StateFlow<TestStats> = _stats.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    init {
        loadRouters()
        loadSettings()
    }

    private fun loadRouters() {
        viewModelScope.launch {
            routerRepository.activeRoutersFlow.collect { routers ->
                _uiState.value = _uiState.value.copy(
                    routers = routers,
                    selectedRouter = routers.firstOrNull()
                )
            }
        }
    }

    private fun loadSettings() {        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    delayMs = settings.delayMs,
                    stopOnSuccess = settings.stopOnSuccess,
                    skipTested = settings.skipTested
                )
            }
        }
    }

    fun updateConfig(config: CardConfig) {
        _uiState.value = _uiState.value.copy(currentConfig = config)
    }

    fun updateStats(stats: TestStats) {
        _stats.value = stats
    }

    fun addLog(message: String, type: TestServiceBridge.LogType) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(LogEntry(message, type, System.currentTimeMillis()))
        // Keep last 100 logs
        if (currentLogs.size > 100) {
            currentLogs.removeAt(0)
        }
        _logs.value = currentLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun selectRouter(routerId: Int) {
        viewModelScope.launch {
            val router = routerRepository.getRouterById(routerId)
            _uiState.value = _uiState.value.copy(
                selectedRouter = router,
                currentConfig = _uiState.value.currentConfig.copy(
                    prefix = router?.macPrefix ?: "",
                    allowedChars = router?.allowedChars ?: "0123456789ABCDEF",
                    length = router?.defaultLength ?: 16
                )
            )
        }
    }
}

data class HomeUiState(
    val routers: List<com.wdmaster.app.data.model.Router> = emptyList(),    val selectedRouter: com.wdmaster.app.data.model.Router? = null,
    val currentConfig: CardConfig = CardConfig(),
    val delayMs: Long = 500L,
    val stopOnSuccess: Boolean = false,
    val skipTested: Boolean = true,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

data class LogEntry(
    val message: String,
    val type: TestServiceBridge.LogType,
    val timestamp: Long
)