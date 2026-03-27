package com.wdmaster.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wdmaster.app.R
import com.wdmaster.app.data.model.TestResult
import com.wdmaster.app.databinding.ItemTestResultBinding

class ResultsAdapter(
    private val onItemClick: (TestResult) -> Unit
) : ListAdapter<TestResult, ResultsAdapter.ResultViewHolder>(DiffCallback()) {
    
    private var successFilter: Boolean? = null
    private var fullList: List<TestResult> = emptyList()
    
    private class DiffCallback : DiffUtil.ItemCallback<TestResult>() {
        override fun areItemsTheSame(oldItem: TestResult, newItem: TestResult) = 
            oldItem.id == newItem.id
        
        override fun areContentsTheSame(oldItem: TestResult, newItem: TestResult) = 
            oldItem == newItem
    }
    
    class ResultViewHolder(
        private val binding: ItemTestResultBinding,
        private val onItemClick: (TestResult) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(result: TestResult) {
            binding.tvCard.text = result.card
            binding.tvTime.text = result.getFormattedTime()
            binding.tvStatus.text = result.getStatusText()
            
            val statusColor = if (result.success) R.color.success_green else R.color.error_red
            binding.tvStatus.setTextColor(binding.root.context.getColor(statusColor))
            
            binding.root.setOnClickListener { onItemClick(result) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemTestResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ResultViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun addResult(result: TestResult) {
        fullList = fullList + result
        applyFilter()
    }
    
    fun filterBySuccess(success: Boolean?) {
        successFilter = success
        applyFilter()
    }
    
    private fun applyFilter() {
        val filtered = when (successFilter) {
            true -> fullList.filter { it.success }
            false -> fullList.filter { !it.success }
            null -> fullList
        }
        submitList(filtered)
    }
    
    fun clearResults() {
        fullList = emptyList()
        submitList(emptyList())
    }
    
    val currentList: List<TestResult> get() = fullList
}