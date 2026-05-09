package com.example.techloan.features.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.databinding.ItemInventoryBinding
import com.example.techloan.shared.model.InventoryItemDto

class InventoryAdapter(
    private var items: List<InventoryItemDto> = emptyList(),
    private val onItemClick: (InventoryItemDto) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.itemName
            tvCategory.text = item.category ?: "General"
            tvQuantity.text = "Available: ${item.availableQuantity}"
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<InventoryItemDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
