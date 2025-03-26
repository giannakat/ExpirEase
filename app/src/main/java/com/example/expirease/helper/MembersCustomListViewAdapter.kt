package com.example.expirease.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.expirease.R
import com.example.expirease.data.Member

class MembersCustomListViewAdapter (private val context: Context, private val memberList: List<Member>): BaseAdapter() {
    override fun getCount(): Int = memberList.size

    override fun getItem(position: Int): Any = memberList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //inflate the view
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.activity_custom_list_view, parent, false)

        val memberPhoto = view.findViewById<ImageView>(R.id.item_photo)
        val memberName = view.findViewById<TextView>(R.id.item_name)
        val item = memberList[position]

        memberPhoto.setImageResource(item.photoResource)
        memberName.setText("$memberName")

        return view


    }

}