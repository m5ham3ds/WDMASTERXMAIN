package com.wdmaster.app.data.model

data class LogItem(
    val message: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)