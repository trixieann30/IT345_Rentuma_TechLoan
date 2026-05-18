package com.example.techloan.features.penalty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.R
import com.example.techloan.databinding.ItemPenaltyBinding
import com.example.techloan.shared.model.PenaltyDto

class PenaltyAdapter(
    private val onPayClick: (PenaltyDto) -> Unit
) : RecyclerView.Adapter<PenaltyAdapter.ViewHolder>() {

    private var items: List<PenaltyDto> = emptyList()

    fun updateItems(newItems: List<PenaltyDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPenaltyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val b: ItemPenaltyBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: PenaltyDto) {
            val ctx = b.root.context
            b.tvItemName.text = p.itemName ?: "Unknown Item"
            b.tvDaysOverdue.text = "${p.daysOverdue}d overdue"
            b.tvAmount.text = "₱%,d.00".format(p.penaltyPoints * 50)

            if (p.paid) {
                b.tvStatus.text = "PAID"
                b.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
                b.tvDaysOverdue.visibility = View.GONE
                b.btnPay.visibility = View.GONE
                b.root.alpha = 0.7f
            } else {
                b.tvStatus.text = "UNPAID"
                b.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
                b.tvDaysOverdue.visibility = View.VISIBLE
                b.btnPay.visibility = View.VISIBLE
                b.btnPay.setOnClickListener { onPayClick(p) }
                b.root.alpha = 1f
            }
        }
    }
}
