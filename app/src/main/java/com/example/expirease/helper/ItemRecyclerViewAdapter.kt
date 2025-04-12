package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item

class ItemRecyclerViewAdapter(private val listOfItems: MutableList<Item>, private val onClick : (Item) -> Unit ): RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val photo = view.findViewById<ImageView>(R.id.item_photo)
        val name = view.findViewById<TextView>(R.id.item_name)
        val quantity = view.findViewById<TextView>(R.id.item_quantity)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemRecyclerViewAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemRecyclerViewAdapter.ItemViewHolder, position: Int) {
        val item = listOfItems[position]

        holder.photo.setImageResource(item.photoResource)
        holder.name.setText(item.name)
        holder.quantity.text = "Quantity: ${item.quantity}"

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = listOfItems.size

    fun updateData(newItems: MutableList<Item>) {
        listOfItems.clear()
        listOfItems.addAll(newItems)
        notifyDataSetChanged()
    }


}