package com.wdmaster.app.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.service.TestServiceBridge
import com.wdmaster.app.data.model.LogItem

class LogDiffCallback : DiffUtil.ItemCallback<LogItem>() {
    
    data class LogItem(
        val id: String,
        val message: String,
        val type: TestServiceBridge.LogType,
        val timestamp: Long
    ) {
        companion object {
            fun fromMessage(message: String, type: TestServiceBridge.LogType): LogItem {
                return LogItem(
                    id = "${System.currentTimeMillis()}_${message.hashCode()}",
                    message = message,
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }
    
    override fun areItemsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
        return oldItem.message == newItem.message &&
               oldItem.type == newItem.type &&
               oldItem.timestamp == newItem.timestamp
    }
    
    override fun getChangePayload(oldItem: LogItem, newItem: LogItem): Any? {
        return if (oldItem.message != newItem.message) {
            PAYLOAD_MESSAGE_CHANGED
        } else null
    }
    
    companion object {
        const val PAYLOAD_MESSAGE_CHANGED = "message_changed"
    }
}
