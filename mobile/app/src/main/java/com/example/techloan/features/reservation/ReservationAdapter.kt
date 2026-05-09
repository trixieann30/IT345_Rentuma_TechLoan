package com.example.techloan.features.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.R
import com.example.techloan.databinding.ItemReservationBinding
import com.example.techloan.shared.model.BorrowRequestDto

class ReservationAdapter(
    private var items: List<BorrowRequestDto> = emptyList(),
    private val onShowQr: (BorrowRequestDto) -> Unit = {}
) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.itemName ?: "Unknown Item"
            tvReturnDate.text = "Return: ${item.returnDate ?: item.dueDate ?: "—"}"

            val (bgColor, textColor) = when (item.status) {
                "APPROVED" -> Pair(R.color.green_50, R.color.green_600)
                "REJECTED" -> Pair(R.color.red_50, R.color.red_400)
                "RETURNED" -> Pair(R.color.blue_50, R.color.blue_600)
                "OVERDUE"  -> Pair(R.color.amber_50, R.color.amber_600)
                else       -> Pair(R.color.amber_50, R.color.amber_600)
            }
            tvStatus.text = item.status ?: "PENDING"
            tvStatus.setBackgroundColor(ContextCompat.getColor(root.context, bgColor))
            tvStatus.setTextColor(ContextCompat.getColor(root.context, textColor))

            if (item.status == "APPROVED") {
                btnShowQr.visibility = View.VISIBLE
                btnShowQr.setOnClickListener { onShowQr(item) }
            } else {
                btnShowQr.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<BorrowRequestDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
