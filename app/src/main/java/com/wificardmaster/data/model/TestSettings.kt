package com.wdmaster.app.data.model

data class TestSettings(
    val delayMs: Long = 500,
    val retryCount: Int = 3,
    val stopOnSuccess: Boolean = false,
    val skipTested: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoExport: Boolean = false,
    val vibrateOnSuccess: Boolean = true,
    val soundOnSuccess: Boolean = false
) {
    companion object {
        val DEFAULT = TestSettings()
    }
}