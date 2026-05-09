package com.example.techloan.features.custodian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.databinding.ItemActiveLoanBinding
import com.example.techloan.shared.model.LoanDto
import android.view.View

class LoanManagementAdapter(
    private var items: List<LoanDto> = emptyList(),
    private val onReturn: (LoanDto) -> Unit
) : RecyclerView.Adapter<LoanManagementAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActiveLoanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveLoanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.itemName ?: "Unknown Item"
            tvBorrowerEmail.text = item.userEmail ?: "—"
            tvDueDate.text = "Due: ${item.dueDate ?: "—"}"
            tvOverdueBadge.visibility = if (item.status == "OVERDUE") View.VISIBLE else View.GONE
            btnReturn.setOnClickListener { onReturn(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<LoanDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
