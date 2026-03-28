package com.wdmaster.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.model.Router
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _routers = MutableStateFlow<List<Router>>(emptyList())
    val routers: StateFlow<List<Router>> = _routers.asStateFlow()

    init {
        loadRouters()
        loadSettings()
    }

    private fun loadRouters() {
        viewModelScope.launch {
            routerRepository.allRoutersFlow.collect { routers ->
                _routers.value = routers
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    darkModeEnabled = settings.darkModeEnabled,
                    vibrateOnSuccess = settings.vibrateOnSuccess,
                    soundOnSuccess = settings.soundOnSuccess,
                    autoExport = settings.autoExport
                )
            }
        }    }

    fun addRouter(router: Router) {
        viewModelScope.launch {
            routerRepository.addRouter(router)
        }
    }

    fun updateRouter(router: Router) {
        viewModelScope.launch {
            routerRepository.updateRouter(router)
        }
    }

    fun deleteRouter(router: Router) {
        viewModelScope.launch {
            routerRepository.deleteRouter(router)
        }
    }

    fun toggleRouterActive(router: Router, active: Boolean) {
        viewModelScope.launch {
            routerRepository.toggleActive(router.id, active)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleDarkMode(enabled)
        }
    }

    fun toggleVibrate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleVibrateOnSuccess(enabled)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleSoundOnSuccess(enabled)
        }
    }
}

data class SettingsUiState(
    val darkModeEnabled: Boolean = false,
    val vibrateOnSuccess: Boolean = true,
    val soundOnSuccess: Boolean = false,
    val autoExport: Boolean = false,    val isLoading: Boolean = false,
    val errorMessage: String? = null
)