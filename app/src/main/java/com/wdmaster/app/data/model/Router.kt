package com.wdmaster.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routers")
data class Router(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val macPrefix: String,
    val chipset: String?,
    val defaultLength: Int = 16,
    val allowedChars: String = "0123456789ABCDEF",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDisplayName(): String = "$name ($macPrefix)"
}