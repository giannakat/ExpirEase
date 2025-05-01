package com.example.expirease.helperNotif

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerViewAdapter(
    private val listOfItems: MutableList<Item>,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<NotificationRecyclerViewAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photo: ImageView = view.findViewById(R.id.item_photo)
        private val name: TextView = view.findViewById(R.id.item_name)
        private val expiry: TextView = view.findViewById(R.id.item_expiryDate)

        fun bind(item: Item, onClick: (Item) -> Unit) {
            photo.setImageResource(item.photoResource)
            name.text = "Urgent!"
            val formattedExpiryDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.expiryDate))
            val expiryText = if (item.expiryDate < System.currentTimeMillis()) {
                "${item.name} has expired on $formattedExpiryDate"
            } else {
                "${item.name} will expire on $formattedExpiryDate"
            }
            expiry.text = expiryText
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notif_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(listOfItems[position], onClick)
    }

    override fun getItemCount(): Int = listOfItems.size
}
