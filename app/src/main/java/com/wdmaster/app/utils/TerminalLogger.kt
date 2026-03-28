package com.wdmaster.app.utils

import com.wdmaster.app.service.TestServiceBridge
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalLogger @Inject constructor() {
    
    private val _logs = MutableSharedFlow<LogEntry>(replay = 50)
    val logs = _logs.asSharedFlow()
    
    data class LogEntry(
        val message: String,
        val type: TestServiceBridge.LogType,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    fun log(message: String, type: TestServiceBridge.LogType = TestServiceBridge.LogType.INFO) {
        kotlinx.coroutines.runBlocking {
            _logs.emit(LogEntry(message, type))
        }
    }
    
    fun info(message: String) = log(message, TestServiceBridge.LogType.INFO)
    fun success(message: String) = log(message, TestServiceBridge.LogType.SUCCESS)
    fun error(message: String) = log(message, TestServiceBridge.LogType.ERROR)
    fun warning(message: String) = log(message, TestServiceBridge.LogType.WARNING)
    fun debug(message: String) = log(message, TestServiceBridge.LogType.DEBUG)
    
    fun clear() {
        // Implement clear logic if needed
    }
}