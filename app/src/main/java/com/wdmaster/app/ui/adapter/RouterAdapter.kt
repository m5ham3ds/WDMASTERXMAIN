package com.wdmaster.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wdmaster.app.R
import com.wdmaster.app.data.model.Router
import com.wdmaster.app.databinding.ItemRouterBinding

class RouterAdapter(
    private val onEdit: (Router) -> Unit,
    private val onDelete: (Router) -> Unit,
    private val onToggle: (Router, Boolean) -> Unit
) : ListAdapter<Router, RouterAdapter.RouterViewHolder>(DiffCallback()) {
    
    private class DiffCallback : DiffUtil.ItemCallback<Router>() {
        override fun areItemsTheSame(oldItem: Router, newItem: Router) = 
            oldItem.id == newItem.id
        
        override fun areContentsTheSame(oldItem: Router, newItem: Router) = 
            oldItem == newItem
    }
    
    class RouterViewHolder(
        private val binding: ItemRouterBinding,
        private val onEdit: (Router) -> Unit,
        private val onDelete: (Router) -> Unit,
        private val onToggle: (Router, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(router: Router) {
            binding.tvRouterName.text = router.name
            binding.tvRouterPrefix.text = router.macPrefix
            binding.tvChipset.text = router.chipset ?: "Unknown"
            binding.swActive.isChecked = router.isActive
            
            binding.swActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(router, isChecked)
            }
            
            binding.btnEdit.setOnClickListener { onEdit(router) }
            binding.btnDelete.setOnClickListener { onDelete(router) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouterViewHolder {
        val binding = ItemRouterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RouterViewHolder(binding, onEdit, onDelete, onToggle)
    }
    
    override fun onBindViewHolder(holder: RouterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}