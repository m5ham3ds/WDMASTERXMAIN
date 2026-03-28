package com.wdmaster.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "successful_patterns")
data class CardPattern(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String,
    val routerId: Int?,
    val successCount: Int = 1,
    val lastUsed: Long = System.currentTimeMillis(),
    val metadata: String? = null
) {
    fun getWeight(): Double {
        val ageInDays = (System.currentTimeMillis() - lastUsed) / 86400000.0
        return successCount * (1.0 + (1.0 / (ageInDays + 1.0)))
    }
    
    fun toCard(allowedChars: String): String {
        return pattern.map { char ->
            if (char == '*' || char == 'X') {
                allowedChars.random()
            } else {
                char
            }
        }.joinToString("")
    }
}