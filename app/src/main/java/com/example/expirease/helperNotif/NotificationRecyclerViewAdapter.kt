package com.example.expirease.helperNotif

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerViewAdapter(
    private val listOfItems: MutableList<Any>, // Accepting mixed types (String headers and Item objects)
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<NotificationRecyclerViewAdapter.ItemViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photo: ImageView = view.findViewById(R.id.notif_photo)
        private val title: TextView = view.findViewById(R.id.notif_title)
        private val details: TextView = view.findViewById(R.id.notif_details)

        fun bind(item: Any, onClick: (Item) -> Unit) {
            if (item is String) {
                // Handle header (String)
                title.text = item
                title.setTextSize(20f) // Make it bold and bigger for header
                details.visibility = View.GONE // Hide details for the header
                photo.visibility = View.GONE  // Hide photo for the header
            } else if (item is Item) {
                // Handle normal item (Item)
                photo.setImageResource(item.photoResource)
                val formattedExpiryDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.expiryDate))
                val name: String
                val msg: String

                if (item.expiryDate < System.currentTimeMillis()) {
                    name = "${item.name} is already expired!"
                    msg = "${item.name} has expired on $formattedExpiryDate. Throw them out to save more space."
                } else {
                    name = "${item.name} is almost expired!"
                    msg = "${item.name} will expire $formattedExpiryDate. Consume or donate them before it's too late."
                }

                title.text = name
                details.text = msg
                photo.visibility = View.VISIBLE
                details.visibility = View.VISIBLE
                itemView.setOnClickListener { onClick(item) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfItems[position] is String) TYPE_HEADER else TYPE_ITEM
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
