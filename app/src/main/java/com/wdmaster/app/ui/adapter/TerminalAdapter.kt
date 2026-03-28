package com.wdmaster.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wdmaster.app.R
import com.wdmaster.app.databinding.ItemTerminalLogBinding
import com.wdmaster.app.service.TestServiceBridge

class TerminalAdapter : ListAdapter<TerminalAdapter.LogItem, TerminalAdapter.LogViewHolder>(DiffCallback()) {
    
    data class LogItem(
        val message: String,
        val type: TestServiceBridge.LogType,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private class DiffCallback : DiffUtil.ItemCallback<LogItem>() {
        override fun areItemsTheSame(oldItem: LogItem, newItem: LogItem) = 
            oldItem.timestamp == newItem.timestamp
        
        override fun areContentsTheSame(oldItem: LogItem, newItem: LogItem) = 
            oldItem == newItem
    }
    
    class LogViewHolder(private val binding: ItemTerminalLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogItem) {
            binding.tvLogMessage.text = item.message
            binding.tvLogTime.text = android.text.format.DateFormat.format("HH:mm:ss", item.timestamp).toString()
            
            val colorRes = when (item.type) {
                TestServiceBridge.LogType.SUCCESS -> R.color.success_green
                TestServiceBridge.LogType.ERROR -> R.color.error_red
                TestServiceBridge.LogType.WARNING -> R.color.warning_yellow
                TestServiceBridge.LogType.DEBUG -> R.color.info_blue
                TestServiceBridge.LogType.INFO -> R.color.text_primary
            }
            binding.tvLogMessage.setTextColor(binding.root.context.getColor(colorRes))
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemTerminalLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun addLog(message: String, type: TestServiceBridge.LogType) {
        val currentList = currentList.toMutableList()
        currentList.add(LogItem(message, type))
        submitList(currentList)
    }
    
    fun clearLogs() {
        submitList(emptyList())
    }
}