package com.example.expirease.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.expirease.R
import com.example.expirease.data.Item

//pass content
class ItemsCustomListViewAdapter(private val context: Context, private val itemList: List<Item>): BaseAdapter() {
    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //inflate the view
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.activity_custom_list_view, parent, false)

        val itemPic = view.findViewById<ImageView>(R.id.item_photo)
        val itemName = view.findViewById<TextView>(R.id.item_name)
        val item = itemList[position]

        itemPic.setImageResource(item.photoResource)
        itemName.setText("$itemName")

        return view
    }
}