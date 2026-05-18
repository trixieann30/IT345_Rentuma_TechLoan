package com.example.techloan.features.custodian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.R
import com.example.techloan.databinding.ItemAdminPenaltyBinding
import com.example.techloan.shared.model.AdminPenaltyDto

class CustodianPenaltyAdapter : RecyclerView.Adapter<CustodianPenaltyAdapter.ViewHolder>() {

    private var items: List<AdminPenaltyDto> = emptyList()

    fun updateItems(newItems: List<AdminPenaltyDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPenaltyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val b: ItemAdminPenaltyBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: AdminPenaltyDto) {
            b.tvUserName.text = p.userName ?: "Unknown"
            b.tvUserEmail.text = p.userEmail ?: ""
            b.tvItemName.text = p.itemName ?: "Unknown Item"
            b.tvDaysOverdue.text = "${p.daysOverdue}d"
            b.tvAmount.text = "₱%,d.00".format(p.penaltyPoints * 50)
            b.tvPenaltyPts.text = "${p.penaltyPoints} pts"
            b.tvDate.text = p.calculatedAt?.take(10) ?: "—"

            if (p.paid) {
                b.tvStatus.text = "PAID"
                b.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
                b.root.alpha = 0.75f
            } else {
                b.tvStatus.text = "UNPAID"
                b.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
                b.root.alpha = 1f
            }
        }
    }
}
