package com.example.expirease.helperHousehold

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.expirease.R
import com.example.expirease.data.Item

class HouseholdCustomListViewAdapter(
    private val context: Context,
    private val itemList: List<Item>
) : BaseAdapter() {

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.activity_custom_list_view, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = itemList[position]
        viewHolder.itemPic.setImageResource(item.photoResource)
        viewHolder.itemName.text = item.name // Fixed incorrect setText usage

        return view
    }

    private class ViewHolder(view: View) {
        val itemPic: ImageView = view.findViewById(R.id.item_photo)
        val itemName: TextView = view.findViewById(R.id.item_name)
    }
}