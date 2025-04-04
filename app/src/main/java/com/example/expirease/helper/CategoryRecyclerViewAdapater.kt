package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item

class CategoryRecyclerViewAdapter(private val categoryList: MutableList<Category>, private val onClick : (Category) -> Unit ): RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryItemCount: TextView = itemView.findViewById(R.id.category_count)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryRecyclerViewAdapter.CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_box_recycler_view, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryRecyclerViewAdapter.CategoryViewHolder, position: Int) {
        val item = categoryList[position]

        holder.categoryName.text = item.displayName
        holder.categoryItemCount.text = item.itemCount.toString()

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = categoryList.size

}