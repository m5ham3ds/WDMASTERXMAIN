package com.wdmaster.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wdmaster.app.data.model.TestSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "wdmaster_settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    
    private object PreferencesKeys {
        val DELAY_MS = longPreferencesKey("delay_ms")
        val RETRY_COUNT = intPreferencesKey("retry_count")
        val STOP_ON_SUCCESS = booleanPreferencesKey("stop_on_success")
        val SKIP_TESTED = booleanPreferencesKey("skip_tested")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val MAX_TRIES = longPreferencesKey("max_tries")
        val AUTO_EXPORT = booleanPreferencesKey("auto_export")
        val VIBRATE_ON_SUCCESS = booleanPreferencesKey("vibrate_on_success")
        val SOUND_ON_SUCCESS = booleanPreferencesKey("sound_on_success")
    }
    
    val settingsFlow: Flow<TestSettings> = context.dataStore.data.map { prefs ->
        TestSettings(
            delayMs = prefs[PreferencesKeys.DELAY_MS] ?: 500L,
            retryCount = prefs[PreferencesKeys.RETRY_COUNT] ?: 3,
            stopOnSuccess = prefs[PreferencesKeys.STOP_ON_SUCCESS] ?: false,
            skipTested = prefs[PreferencesKeys.SKIP_TESTED] ?: true,
            darkModeEnabled = prefs[PreferencesKeys.DARK_MODE] ?: false,
            autoExport = prefs[PreferencesKeys.AUTO_EXPORT] ?: false,
            vibrateOnSuccess = prefs[PreferencesKeys.VIBRATE_ON_SUCCESS] ?: true,
            soundOnSuccess = prefs[PreferencesKeys.SOUND_ON_SUCCESS] ?: false
        )
    }
    
    suspend fun updateDelay(delayMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.DELAY_MS] = delayMs }
    }
    
    suspend fun updateRetryCount(count: Int) {
        context.dataStore.edit { it[PreferencesKeys.RETRY_COUNT] = count }
    }
    
    suspend fun toggleStopOnSuccess(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STOP_ON_SUCCESS] = enabled }
    }
    
    suspend fun toggleSkipTested(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SKIP_TESTED] = enabled }
    }
    
    suspend fun toggleDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = enabled }
    }
    
    suspend fun updateMaxTries(maxTries: Long) {
        context.dataStore.edit { it[PreferencesKeys.MAX_TRIES] = maxTries }
    }
    
    suspend fun toggleAutoExport(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_EXPORT] = enabled }
    }
    
    suspend fun toggleVibrateOnSuccess(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATE_ON_SUCCESS] = enabled }
    }
    
    suspend fun toggleSoundOnSuccess(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ON_SUCCESS] = enabled }
    }
    
    suspend fun updateAllSettings(settings: TestSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DELAY_MS] = settings.delayMs
            prefs[PreferencesKeys.RETRY_COUNT] = settings.retryCount
            prefs[PreferencesKeys.STOP_ON_SUCCESS] = settings.stopOnSuccess
            prefs[PreferencesKeys.SKIP_TESTED] = settings.skipTested
            prefs[PreferencesKeys.DARK_MODE] = settings.darkModeEnabled
            prefs[PreferencesKeys.AUTO_EXPORT] = settings.autoExport
            prefs[PreferencesKeys.VIBRATE_ON_SUCCESS] = settings.vibrateOnSuccess
            prefs[PreferencesKeys.SOUND_ON_SUCCESS] = settings.soundOnSuccess
        }
    }
}