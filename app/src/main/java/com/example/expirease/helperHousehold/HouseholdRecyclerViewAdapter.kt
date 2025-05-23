package com.example.expirease.helperHousehold

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Member

class HouseholdRecyclerViewAdapter(
    private val listOfItems: MutableList<Member>,
    private val onClick: (Member) -> Unit
) : RecyclerView.Adapter<HouseholdRecyclerViewAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.item_photo)
        val username: TextView = view.findViewById(R.id.member_username)
        val email: TextView = view.findViewById(R.id.member_email)

        fun bind(item: Member, onClick: (Member) -> Unit) {
            photo.setImageResource(item.photoResource)
            username.text = item.username
            email.text = item.email
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.household_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(listOfItems[position], onClick) // Used bind() for cleaner binding logic
    }

    override fun getItemCount(): Int = listOfItems.size
}
