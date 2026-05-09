package com.example.techloan.features.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.databinding.ItemLoanBinding
import com.example.techloan.shared.model.LoanDto

class LoanAdapter(
    private var loans: List<LoanDto> = emptyList()
) : RecyclerView.Adapter<LoanAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLoanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loan = loans[position]
        holder.binding.apply {
            tvItemName.text = loan.itemName ?: "Unknown Item"
            tvDueDate.text = "Due: ${loan.dueDate ?: "—"}"
            tvBorrowedDate.text = "Borrowed: ${loan.borrowedDate ?: "—"}"
            tvOverdueBadge.visibility = if (loan.status == "OVERDUE") View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount() = loans.size

    fun updateLoans(newLoans: List<LoanDto>) {
        loans = newLoans
        notifyDataSetChanged()
    }
}
