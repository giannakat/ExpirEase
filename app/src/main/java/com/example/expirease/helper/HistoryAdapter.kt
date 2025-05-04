package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var items: List<Item>,
    private val onRestore: (Item) -> Unit,
    private val onDelete: (Item) -> Unit
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
        holder.itemAction.text = item.status.toString()

        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate = displayFormat.format(Date(item.expiryDate))
        holder.itemDate.text = "Expiry: $formattedDate"

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Options for: ${item.name}")
                .setItems(arrayOf("Restore", "Delete")) { _, which ->
                    when (which) {
                        0 -> { // Restore
                            if (item.status != ItemStatus.EXPIRED) {
                                AlertDialog.Builder(holder.itemView.context)
                                    .setTitle("Restore Item")
                                    .setMessage("Are you sure you want to restore \"${item.name}\"?")
                                    .setPositiveButton("Yes") { _, _ -> onRestore(item) }
                                    .setNegativeButton("No", null)
                                    .show()
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Cannot restore expired item.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        1 -> {
                            // Directly call delete without dialog
                            onDelete(item)
                        }
                    }
                }
                .show()
            true
        }
    }

    fun submitList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
