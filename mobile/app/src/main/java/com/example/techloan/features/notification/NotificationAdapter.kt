package com.example.techloan.features.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.techloan.R
import com.example.techloan.shared.model.NotificationDto

class NotificationAdapter(
    private val onItemClick: (NotificationDto) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val items = mutableListOf<NotificationDto>()

    fun updateItems(newItems: List<NotificationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView   = view.findViewById(R.id.cardNotification)
        val tvTitle: TextView  = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView   = view.findViewById(R.id.tvNotifTime)
        val unreadDot: View   = view.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text   = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text    = item.createdAt?.take(16)?.replace("T", " ") ?: ""
        holder.unreadDot.visibility = if (item.read) View.GONE else View.VISIBLE
        holder.card.setCardBackgroundColor(
            if (item.read) 0xFFFFFFFF.toInt() else 0xFFEFF6FF.toInt()
        )
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
