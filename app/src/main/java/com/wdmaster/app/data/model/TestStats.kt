package com.wdmaster.app.data.model

data class TestStats(
    val tested: Long = 0,
    val success: Long = 0,
    val failure: Long = 0,
    val progress: Float = 0f,
    val speed: Double = 0.0,
    val etaSeconds: Long = 0,
    val currentCard: String = "",
    val startTime: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
) {
    fun getSuccessRate(): Int {
        return if (tested > 0) ((success * 100) / tested).toInt() else 0
    }
    
    fun getFormattedSpeed(): String {
        return String.format("%.2f", speed)
    }
    
    fun getFormattedETA(): String {
        return when {
            etaSeconds < 60 -> "${etaSeconds}s"
            etaSeconds < 3600 -> "${etaSeconds / 60}m ${etaSeconds % 60}s"
            else -> "${etaSeconds / 3600}h ${(etaSeconds % 3600) / 60}m"
        }
    }
}