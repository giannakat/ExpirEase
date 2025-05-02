package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item
import com.example.expirease.data.ItemStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = displayFormat.format(Date(item.expiryDate))
        holder.itemDate.text = "Expiry: " + formattedDate
//        holder.itemDate.text = item.expiryDate.toString() ?: "No date"

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Product: " + item.name)
                .setMessage(
                    if (item.status == ItemStatus.EXPIRED)
                        "This item has expired and cannot be restored."
                    else
                        "Do you want to restore this item?"
                )
                .setPositiveButton("Restore") { dialog, _ ->
                    if (item.status != ItemStatus.EXPIRED) {
                        onRestore(item)
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Cannot restore expired item.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .apply {
                    if (item.status == ItemStatus.EXPIRED) {
                        // Disable restore button if expired
                        setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        setNegativeButton(null, null)
                    }
                }
                .show()
            true
        }
    }

    // Now submitList is outside onBindViewHolder
    fun submitList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
