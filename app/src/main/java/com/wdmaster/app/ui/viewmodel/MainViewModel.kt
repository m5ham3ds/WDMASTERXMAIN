package com.wdmaster.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.data.model.TestSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(TestSettings.DEFAULT)
    val settings: StateFlow<TestSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _settings.value = settings
                _uiState.value = MainUiState(
                    darkModeEnabled = settings.darkModeEnabled
                )
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleDarkMode(enabled)
        }
    }

    fun refreshConnectionStatus() {
        viewModelScope.launch {
            // Check WiFi connection status
            _uiState.value = _uiState.value.copy(
                isConnected = true // Placeholder
            )
        }
    }
}

data class MainUiState(
    val isConnected: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)