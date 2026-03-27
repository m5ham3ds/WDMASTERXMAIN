package com.wdmaster.app.data.model

data class CardConfig(
    val prefix: String = "",
    val length: Int = 16,
    val allowedChars: String = "0123456789ABCDEF",
    val maxTries: Long = 1000,
    val useLearnedPatterns: Boolean = true
) {
    fun validate(): Boolean {
        return length > 0 && length <= 64 && maxTries > 0 && allowedChars.isNotEmpty()
    }
    
    fun getRemainingLength(): Int = length - prefix.length
}