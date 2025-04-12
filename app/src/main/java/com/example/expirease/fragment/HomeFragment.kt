package com.example.expirease.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helper.CategoryRecyclerViewAdapter
import com.example.expirease.helper.ItemRecyclerViewAdapter
import com.example.expirease.helper.OnItemUpdatedListener
import com.example.expirease.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    lateinit var listOfItems: MutableList<Item>
    lateinit var filteredList: MutableList<Item>
    lateinit var itemAdapter: ItemRecyclerViewAdapter
    lateinit var searchView: SearchView
    private lateinit var sharedViewModel: SharedViewModel
    val categoryList = Category.values().toMutableList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Initialize lists first
        listOfItems = mutableListOf()
        filteredList = mutableListOf()

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // RecyclerView setup
        val itemRecyclerView = view.findViewById<RecyclerView>(R.id.item_recyclerview)
        itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemAdapter = ItemRecyclerViewAdapter(filteredList, onClick = { item ->
            val bottomSheet = EditItemBottomSheet()
            val bundle = Bundle().apply {
                putInt("photo", item.photoResource)
                putString("name", item.name)
                putInt("quantity", item.quantity)
                putLong("expiryDate", item.expiryDate)
                putString("category", item.category.toString())
            }
            bottomSheet.arguments = bundle

            bottomSheet.onItemUpdatedListener = object : OnItemUpdatedListener {
                override fun onItemUpdated(name: String, quantity: Int, expiryDate: Long, category: String) {
                    item.name = name
                    item.quantity = quantity
                    item.expiryDate = expiryDate
                    item.category = Category.valueOf(category.uppercase())

                    val updatedList = sharedViewModel.items.value ?: mutableListOf()
                    val index = updatedList.indexOf(item)
                    if (index >= 0) {
                        updatedList[index] = item
                        sharedViewModel.items.value = updatedList
                    }

                    itemAdapter.notifyDataSetChanged()
                }
            }

            bottomSheet.show(parentFragmentManager, "EditItemBottomSheet")
        })
        itemRecyclerView.adapter = itemAdapter

        // LiveData observer
        sharedViewModel.items.observe(viewLifecycleOwner) { items ->
            listOfItems.clear()
            listOfItems.addAll(items)

            filteredList.clear()
            filteredList.addAll(items)

            itemAdapter.notifyDataSetChanged()
        }

        // Example item (for testing)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val newItem = Item("Egg", 2, dateFormat.parse("2025-04-05")!!.time, Category.BAKERY, R.drawable.img_product_banana)
        sharedViewModel.addItem(newItem)

        // SearchView setup
        searchView = view.findViewById(R.id.search_bar)
        setupSearchView()

        // Add Item Button
        val buttonAdd = view.findViewById<ImageView>(R.id.add_button)
        buttonAdd.setOnClickListener {
            showAddItemDialog()
        }

        // Category RecyclerView
        val categoryRecyclerView = view.findViewById<RecyclerView>(R.id.category_recyclerview)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val categoryAdapter = CategoryRecyclerViewAdapter(categoryList) { category ->
            Toast.makeText(requireContext(), "Clicked category: ${category.displayName}", Toast.LENGTH_SHORT).show()
            filterItemsByCategory(category)
        }
        categoryRecyclerView.adapter = categoryAdapter

        return view
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

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
            val lowerQuery = query.lowercase()
            filteredList.addAll(listOfItems.filter { it.name.lowercase().contains(lowerQuery) })
        }
        itemAdapter.notifyDataSetChanged()
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
        val editItemName = dialogView.findViewById<EditText>(R.id.edit_item_name)
        val editItemQuantity = dialogView.findViewById<EditText>(R.id.edit_item_quantity)
        val btnIncrease = dialogView.findViewById<Button>(R.id.btn_increase)
        val btnDecrease = dialogView.findViewById<Button>(R.id.btn_decrease)
        val tvExpiry = dialogView.findViewById<TextView>(R.id.tv_expiry)
        val btnPickDate = dialogView.findViewById<ImageButton>(R.id.btn_pick_date)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        var selectedExpiryDate: Long = System.currentTimeMillis()

        val btnAdd = dialogView.findViewById<Button>(R.id.btn_add_item)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList.map { it.displayName })
        spinnerCategory.adapter = spinnerAdapter

        btnIncrease.setOnClickListener {
            val current = editItemQuantity.text.toString().toIntOrNull() ?: 1
            editItemQuantity.setText((current + 1).toString())
        }

        btnDecrease.setOnClickListener {
            val current = editItemQuantity.text.toString().toIntOrNull() ?: 1
            if (current > 1) {
                editItemQuantity.setText((current - 1).toString())
            }
        }

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                selectedExpiryDate = calendar.timeInMillis
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvExpiry.text = dateFormat.format(Date(selectedExpiryDate))
            }, year, month, day)

            datePicker.show()
        }

        btnAdd.setOnClickListener {
            val name = editItemName.text.toString()
            val quantity = editItemQuantity.text.toString().toIntOrNull() ?: 1
            val selectedCategoryName = spinnerCategory.selectedItem.toString()
            val selectedCategory = Category.values().find { it.displayName == selectedCategoryName }

            if (name.isNotEmpty() && selectedCategory != null) {
                addItem(name, quantity, selectedExpiryDate, selectedCategory, R.drawable.img_product_banana)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter all item details.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addItem(name: String, quantity: Int, expiryDate: Long, selectedCategory: Category, img: Int) {
        val newItem = Item(name, quantity, expiryDate, selectedCategory, img)
        listOfItems.add(newItem)
        filteredList.add(newItem)

        sharedViewModel.addItem(newItem)
        itemAdapter.notifyItemInserted(filteredList.size - 1)
    }

    private fun filterItemsByCategory(category: Category) {
        filteredList.clear()
        if (category == Category.OTHER) {
            filteredList.addAll(listOfItems)
        } else {
            filteredList.addAll(listOfItems.filter { it.category == category })
        }
        itemAdapter.notifyDataSetChanged()
    }
}
