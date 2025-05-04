package com.example.expirease.helper

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ItemRecyclerViewAdapter(private val listOfItems: MutableList<Item>, private val onClick : (Item) -> Unit, private val onLongClick : (Item) -> Unit ): RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val photo = view.findViewById<ImageView>(R.id.item_photo)
        val name = view.findViewById<TextView>(R.id.item_name)
        val quantity = view.findViewById<TextView>(R.id.item_quantity)
        val itemStatus = view.findViewById<TextView>(R.id.item_status)

        fun bind(item: Item) {
            // set text, image, etc.
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = listOfItems[position]
        holder.bind(item)

        holder.photo.setImageResource(item.photoResource)
        holder.name.setText(item.name)
        holder.quantity.text = "Quantity: ${item.quantity}"

        val currentDate = Date()
        val expiryDate = Date(item.expiryDate)

        val diff = expiryDate.time - currentDate.time
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diff)

        val (statusText, cardColor) = when {
            daysRemaining <= 0 -> "Expired" to Color.parseColor("#FFCDD2")  // Red
            daysRemaining <= 3 -> "Expiring" to Color.parseColor("#FFF9C4") // Yellow
            else -> "Fresh" to Color.parseColor("#C8E6C9")               // Green
        }

        holder.cardView.setCardBackgroundColor(cardColor)
        holder.itemStatus.text = statusText
        holder.itemStatus.setBackgroundColor(cardColor)

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = listOfItems.size

    fun updateData(newItems: MutableList<Item>) {
        listOfItems.clear()
        listOfItems.addAll(newItems)
        notifyDataSetChanged()
    }


}