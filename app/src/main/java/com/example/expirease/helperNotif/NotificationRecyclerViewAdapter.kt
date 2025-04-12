package com.example.expirease.helperNotif

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item

class NotificationRecyclerViewAdapter(
    private val listOfItems: MutableList<Item>,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<NotificationRecyclerViewAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.item_photo)
        val name: TextView = view.findViewById(R.id.item_name)
        val expiry: TextView = view.findViewById(R.id.item_expiryDate)

        fun bind(item: Item, onClick: (Item) -> Unit) {
            photo.setImageResource(item.photoResource)
            name.text = item.name
            expiry.text = "Expiry: ${item.expiryDate}"
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notif_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(listOfItems[position], onClick) // Used bind() for cleaner binding logic
    }

    override fun getItemCount(): Int = listOfItems.size
}
