package com.example.techloan.features.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.techloan.databinding.ItemInventoryBinding
import com.example.techloan.shared.model.InventoryItemDto

class InventoryAdapter(
    private var items: MutableList<InventoryItemDto> = mutableListOf(),
    private val onItemClick: (InventoryItemDto) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    // imageUrl overrides fetched via autoImage endpoint
    private val imageOverrides = mutableMapOf<Long, String>()

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

            val url = imageOverrides[item.id]
                ?: item.imageUrl?.takeIf { it.isNotBlank() }

            if (url != null) {
                ivItemImage.visibility = View.VISIBLE
                flImagePlaceholder.visibility = View.GONE
                Glide.with(ivItemImage.context)
                    .load(url)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(ivItemImage)
            } else {
                ivItemImage.visibility = View.GONE
                flImagePlaceholder.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<InventoryItemDto>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun updateImage(id: Long, imageUrl: String) {
        imageOverrides[id] = imageUrl
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) notifyItemChanged(index)
    }
}
