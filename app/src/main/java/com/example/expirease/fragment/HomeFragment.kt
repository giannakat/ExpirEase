package com.example.expirease.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helper.ItemDetailsDialogFragment
import com.example.expirease.helper.ItemRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(){
    lateinit var listOfItems : MutableList<Item>
    lateinit var itemAdapter : ItemRecyclerViewAdapter
    lateinit var filteredList: MutableList<Item>
    lateinit var searchView: SearchView
    val categoryList = mutableListOf(*Category.values().map { it.displayName }.toTypedArray())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View?{
        //equivalent to setContent
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        listOfItems = mutableListOf(
            Item("Egg", 2, dateFormat.parse("2025-04-05")!!.time, "Fruit", R.drawable.banana),
            Item("Milk", 1, dateFormat.parse("2025-04-03")!!.time, "Fruit", R.drawable.banana),
            Item("Bread", 3, dateFormat.parse("2025-04-07")!!.time, "Fruit", R.drawable.banana),
            Item("Rice", 5, dateFormat.parse("2025-04-20")!!.time, "Fruit", R.drawable.banana),
            Item("Apple", 4, dateFormat.parse("2025-04-10")!!.time, "Fruit", R.drawable.banana),
            Item("Chicken", 2, dateFormat.parse("2025-04-04")!!.time, "Fruit", R.drawable.banana),
            Item("Fish", 3, dateFormat.parse("2025-04-06")!!.time, "Fruit", R.drawable.banana),
            Item("Carrot", 6, dateFormat.parse("2025-04-15")!!.time, "Fruit", R.drawable.banana),
            Item("Potato", 7, dateFormat.parse("2025-04-18")!!.time, "Fruit", R.drawable.banana),
            Item("Tomato", 3, dateFormat.parse("2025-04-12")!!.time, "Fruit", R.drawable.banana),
            Item("Onion", 4, dateFormat.parse("2025-04-17")!!.time, "Fruit", R.drawable.banana),
            Item("Garlic", 2, dateFormat.parse("2025-04-22")!!.time, "Fruit", R.drawable.banana),
            Item("Cheese", 1, dateFormat.parse("2025-04-08")!!.time, "Fruit", R.drawable.banana),
            Item("Butter", 2, dateFormat.parse("2025-04-11")!!.time, "Fruit", R.drawable.banana),
            Item("Yogurt", 3, dateFormat.parse("2025-04-05")!!.time, "Fruit", R.drawable.banana)
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
                putLong("expiryDate", item.expiryDate)
                putString("category", item.category)
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
        val tvExpiry = dialogView.findViewById<TextView>(R.id.tv_expiry)
        val btnPickDate = dialogView.findViewById<Button>(R.id.btn_pick_date)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        var selectedExpiryDate: Long = System.currentTimeMillis()

        //set up spinner adapter
        //TODO create custom spinner_item layout
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList)
        spinnerCategory.adapter = spinnerAdapter

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            //box of the date picker
            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                selectedExpiryDate = calendar.timeInMillis

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvExpiry.text = dateFormat.format(Date(selectedExpiryDate))
            }, year, month, day)

            datePicker.show()
        }

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Add New Item")
        dialog.setView(dialogView)
        dialog.setPositiveButton("Add") { _, _ ->
            val name = editItemName.text.toString()
            val quantity = editItemQuantity.text.toString().toIntOrNull() ?: 1  // Default to 1 if empty
            val selectedCategory = spinnerCategory.selectedItem.toString()

            if(!name.isNullOrEmpty()) {
                addItem(name, quantity, selectedExpiryDate, selectedCategory, R.drawable.banana)  // Add new item
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.create().show()
    }

    fun addItem(name: String, quantity: Int, expiryDate: Long, selectedCategory: String, img: Int){
        val newItem = Item(name, quantity, expiryDate, selectedCategory, img)
        listOfItems.add(newItem)
        itemAdapter.notifyItemInserted(listOfItems.size - 1);
    }

    fun isExpired(expiryDate: Long): Boolean{
        val today = System.currentTimeMillis()
        return expiryDate < today
    }

    fun isExpiringSoon(expiryDate: Long): Boolean{
        val today = System.currentTimeMillis()
        val threeDaysLater = today + (3*24*60*60*1000)
        return expiryDate in today..threeDaysLater
    }
}