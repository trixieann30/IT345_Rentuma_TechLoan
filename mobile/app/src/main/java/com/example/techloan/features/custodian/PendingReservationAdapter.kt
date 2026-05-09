package com.example.techloan.features.custodian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.databinding.ItemPendingReservationBinding
import com.example.techloan.shared.model.BorrowRequestDto

class PendingReservationAdapter(
    private var items: List<BorrowRequestDto> = emptyList(),
    private val onApprove: (BorrowRequestDto) -> Unit,
    private val onReject: (BorrowRequestDto) -> Unit
) : RecyclerView.Adapter<PendingReservationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPendingReservationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingReservationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.itemName ?: "Unknown Item"
            tvQuantity.text = "Qty: ${item.quantity}"
            tvBorrowerEmail.text = item.userEmail ?: "—"
            tvPurpose.text = "Purpose: ${item.purpose ?: "—"}"
            tvReturnDate.text = "Return by: ${item.returnDate ?: item.dueDate ?: "—"}"
            btnApprove.setOnClickListener { onApprove(item) }
            btnReject.setOnClickListener { onReject(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<BorrowRequestDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
