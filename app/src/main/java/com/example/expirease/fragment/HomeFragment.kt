package com.example.expirease.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Item
import com.example.expirease.helper.ItemDetailsDialogFragment
import com.example.expirease.helper.ItemRecyclerViewAdapter

class HomeFragment : Fragment(){
    lateinit var listOfItems : MutableList<Item>
    lateinit var itemAdapter : ItemRecyclerViewAdapter
    lateinit var filteredList: MutableList<Item>
    lateinit var searchView: SearchView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View?{
        //equivalent to setContent
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        listOfItems = mutableListOf(
            Item("Egg", 2, R.drawable.banana),
            Item("Milk", 1, R.drawable.banana),
            Item("Bread", 3, R.drawable.banana),
            Item("Rice", 5, R.drawable.banana),
            Item("Apple", 4, R.drawable.banana),
            Item("Chicken", 2, R.drawable.banana),
            Item("Fish", 3, R.drawable.banana),
            Item("Carrot", 6, R.drawable.banana),
            Item("Potato", 7, R.drawable.banana),
            Item("Tomato", 3, R.drawable.banana),
            Item("Onion", 4, R.drawable.banana),
            Item("Garlic", 2, R.drawable.banana),
            Item("Cheese", 1, R.drawable.banana),
            Item("Butter", 2, R.drawable.banana),
            Item("Yogurt", 3, R.drawable.banana)
        )

        // for searching
        filteredList = listOfItems.toMutableList() // initialize filtered list to the current list

        val recyclerView = view.findViewById<RecyclerView>(R.id.item_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemAdapter = ItemRecyclerViewAdapter(filteredList, onClick = { item ->
            val dialog = ItemDetailsDialogFragment()
            val bundle = Bundle().apply {
                putInt("photo", item.photoResource)
                putString("name", item.name)
                putInt("quantity", item.quantity)
            }
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "ItemDetailsDialog")
        })


        recyclerView.adapter = itemAdapter

        //search view
        searchView = view.findViewById<SearchView>(R.id.search_bar)
        setupSearchView()

        val button_add = view.findViewById<ImageView>(R.id.add_button)
        button_add.setOnClickListener {
            showAddItemDialog()
            Toast.makeText(requireContext(), "button add clicked", Toast.LENGTH_LONG).show()
        }


        return view
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No action needed on submit
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return true
            }
        })
    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(listOfItems)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredList.addAll(listOfItems.filter { it.name.lowercase().contains(lowerCaseQuery) })
        }
        itemAdapter.notifyDataSetChanged()
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val editItemName = dialogView.findViewById<EditText>(R.id.edit_item_name)
        val editItemQuantity = dialogView.findViewById<EditText>(R.id.edit_item_quantity)

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Add New Item")
        dialog.setView(dialogView)
        dialog.setPositiveButton("Add") { _, _ ->
            val name = editItemName.text.toString()
            val quantity = editItemQuantity.text.toString().toIntOrNull() ?: 1  // Default to 1 if empty
            if(!name.isNullOrEmpty()) {
                addItem(name, quantity, R.drawable.banana)  // Add new item
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.create().show()
    }

    fun addItem(name: String, quantity: Int, img: Int){
        val newItem = Item(name, quantity, img)
        listOfItems.add(newItem)
        itemAdapter.notifyItemInserted(listOfItems.size - 1);
    }
}