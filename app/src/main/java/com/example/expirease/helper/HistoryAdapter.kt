package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item

class HistoryAdapter(
    private var items: List<Item>, // make it mutable through a setter
    private val onRestore: (Item) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.history_item_name)
        val itemAction: TextView = itemView.findViewById(R.id.history_item_action)
        val itemDate: TextView = itemView.findViewById(R.id.history_item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemAction.text = item.status.toString() ?: "Unknown"
        holder.itemDate.text = item.expiryDate.toString() ?: "No date"

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(holder.itemView.context, it)
            popup.menu.add("Restore")
            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "Restore") {
                    onRestore(item)
                    true
                } else false
            }
            popup.show()
            true
        }
    }

    // Now submitList is outside onBindViewHolder
    fun submitList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
