package com.example.expirease.helper

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item

class CategoryRecyclerViewAdapter(
    private val categoryList: MutableList<Category>,
    var selectedCategoryId: String? = null,
    private val onClick : (Category) -> Unit
): RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryItemCount: TextView = itemView.findViewById(R.id.category_count)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val container: View = itemView.findViewById(R.id.category_container)

        fun bind(category: Category, isSelected: Boolean) {
            categoryName.text = category.displayName
            categoryItemCount.text = category.itemCount.toString()
            cardView.setCardBackgroundColor(category.backgroundColor)

            container.setBackground(
                ContextCompat.getDrawable(
                    itemView.context,
                    if (isSelected) R.drawable.selected_border else R.drawable.normal_background
                )
            )
        }
    }
    fun updateSelectedCategoryId(id: String?) {
        selectedCategoryId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_box_recycler_view, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryList[position]
        val isSelected = item.id == selectedCategoryId
        holder.bind(item, isSelected)
//        holder.categoryName.text = item.displayName
//        holder.categoryItemCount.text = item.itemCount.toString()
//        holder.cardView.setCardBackgroundColor(item.backgroundColor)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = categoryList.size

}