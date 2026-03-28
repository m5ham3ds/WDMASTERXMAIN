package com.wdmaster.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val card: String,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val routerId: Int?,
    val responseTime: Long = 0,
    val errorCode: String? = null
) {
    fun getFormattedTime(): String {
        return android.text.format.DateFormat.format("HH:mm:ss", timestamp).toString()
    }
    
    fun getStatusText(): String = if (success) "SUCCESS" else "FAILED"
}