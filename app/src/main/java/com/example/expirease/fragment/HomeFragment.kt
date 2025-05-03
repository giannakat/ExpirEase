package com.example.expirease.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.CategoryManager
import com.example.expirease.data.Item
import com.example.expirease.data.ItemStatus
import com.example.expirease.helper.CategoryRecyclerViewAdapter
import com.example.expirease.helper.ItemRecyclerViewAdapter
import com.example.expirease.helper.OnItemUpdatedListener
import com.example.expirease.viewmodel.SharedItemViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryRecyclerViewAdapter
    private val sharedItemViewModel: SharedItemViewModel by activityViewModels()

    private lateinit var itemAdapter: ItemRecyclerViewAdapter
    private lateinit var filteredList: MutableList<Item>
    private lateinit var listOfItems: MutableList<Item>

    private lateinit var searchView: SearchView
    private val categoryList = CategoryManager.getCategories().toMutableList()
    private var selectedCategoryId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        listOfItems = mutableListOf()
        filteredList = mutableListOf()

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setupRecyclerView(view)
        setupSearchView(view)
        setupAddButton(view)
        setupCategoryRecyclerView(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryRecyclerView = view.findViewById(R.id.category_recyclerview)

        sharedItemViewModel.allItems.observe(viewLifecycleOwner) { updatedList ->
            val activeItems = updatedList.filter { it.status == ItemStatus.ACTIVE || it.status == ItemStatus.EXPIRED }
            val sortedItems = activeItems.sortedBy { it.expiryDate }

            listOfItems.clear()
            listOfItems.addAll(activeItems)

            filteredList.clear()
            filteredList.addAll(sortedItems)

            itemAdapter.notifyDataSetChanged()
            updateCategoryItemCounts()
            categoryAdapter.notifyDataSetChanged()

            updateUI(sortedItems)
        }

    }

    private fun setupRecyclerView(view: View) {
        val itemRecyclerView = view.findViewById<RecyclerView>(R.id.item_recyclerview)
        itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemAdapter = ItemRecyclerViewAdapter(
            filteredList,
            onClick = { item -> openEditItemBottomSheet(item) },
            onLongClick = { item -> showItemOptionsDialog(item) }
        )

        itemRecyclerView.adapter = itemAdapter

        // 🚀 Swipe left to consume
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = filteredList[position]

                AlertDialog.Builder(requireContext())
                    .setTitle("Consume Item")
                    .setMessage("Are you sure you want to consume \"${item.name}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        consumeItem(item)
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Restore the swiped item visually
                        itemAdapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        // Handle back press/cancel to restore item
                        itemAdapter.notifyItemChanged(position)
                    }
                    .show()
            }

        })

        itemTouchHelper.attachToRecyclerView(itemRecyclerView)
    }

    private fun updateCategoryItemCounts() {
        // Reset all category counts to 0
        categoryList.forEach { it.itemCount = 0 }

        // Count items for each category
        listOfItems.forEach { item ->
            val category = categoryList.find { it.id.equals(item.categoryId, ignoreCase = true) }

            if (category != null) {
                category.itemCount++
            } else {
                // Handle invalid category
                println("CategoryWarning: Item '${item.name}' has an invalid category '${item.categoryId}'")
            }
        }

        // Update the adapter to reflect changes
        categoryAdapter.notifyDataSetChanged()
    }

    private fun updateUI(itemList: List<Item>) {
        val textViewNoItems = view?.findViewById<TextView>(R.id.textViewNoItems)
        val recyclerViewItems = view?.findViewById<RecyclerView>(R.id.item_recyclerview)

        if (itemList.isEmpty()) {
            textViewNoItems?.visibility = View.VISIBLE
            recyclerViewItems?.visibility = View.GONE
        } else {
            textViewNoItems?.visibility = View.GONE
            recyclerViewItems?.visibility = View.VISIBLE
        }
    }

    private fun setupSearchView(view: View) {
        searchView = view.findViewById(R.id.search_bar)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?) : Boolean {
                filterList(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupAddButton(view: View) {
        val buttonAdd = view.findViewById<ImageView>(R.id.add_button)
        buttonAdd.setOnClickListener {
            showAddItemDialog()
            Toast.makeText(requireContext(), "Add button clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryRecyclerView(view: View) {
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        categoryAdapter = CategoryRecyclerViewAdapter(categoryList, selectedCategoryId) { category ->
            Toast.makeText(requireContext(), "Clicked category: ${category.displayName}", Toast.LENGTH_SHORT).show()
            if (category.id == categoryAdapter.selectedCategoryId) {
                // Deselect if clicked again
                categoryAdapter.updateSelectedCategoryId(null)
                filterItems(null) // or show all items
            } else {
                // Select the new one
                categoryAdapter.updateSelectedCategoryId(category.id)
                filterItems(category)
            }
        }

        categoryRecyclerView.adapter = categoryAdapter
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

        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(true)
        }

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryList.map { it.displayName }
        )
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
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedExpiryDate = calendar.timeInMillis
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvExpiry.text = displayFormat.format(Date(selectedExpiryDate))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialogView.findViewById<Button>(R.id.btn_add_item).setOnClickListener {
            val name = editItemName.text.toString()
            val quantity = editItemQuantity.text.toString().toIntOrNull() ?: 1
            val selectedCategoryName = spinnerCategory.selectedItem.toString()
            val selectedCategory = categoryList.find { it.displayName == selectedCategoryName }

            if (name.isNotEmpty() && selectedCategory != null) {
                val imageResId = getImageForCategory(selectedCategory)
                addItem(name, quantity, selectedExpiryDate, selectedCategory, imageResId)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getImageForCategory(category: Category): Int {
        return when (category.displayName.lowercase()) {
            "fruits" -> R.drawable.img_product_banana
            "vegetables" -> R.drawable.img_category_vegetable
            "dairy" -> R.drawable.img_category_dairy
            "meat" -> R.drawable.img_category_meat
            "beverages" -> R.drawable.img_category_beverage
            else -> R.drawable.img_category_others
        }
    }

    private fun addItem(name: String, quantity: Int, expiryDate: Long, selectedCategory: Category, img: Int) {
        val newItem = Item(name, quantity, ItemStatus.ACTIVE, expiryDate, selectedCategory.toString(), img)
        val currentTime = System.currentTimeMillis()
        newItem.status = if (expiryDate < currentTime) ItemStatus.EXPIRED else ItemStatus.ACTIVE

        sharedItemViewModel.addItem(newItem)
        updateCategoryItemCounts()
    }

    private fun filterItems(category: Category?) {
        filteredList.clear()

        if (category == null) {
            // Show all items when no category is selected
            filteredList.addAll(listOfItems)
        } else {
            filteredList.addAll(
                listOfItems.filter { item ->
                    item.categoryId.equals(category.displayName, ignoreCase = true)
                }
            )
        }
        itemAdapter.notifyDataSetChanged()
        updateUI(filteredList)
    }

    private fun openEditItemBottomSheet(item: Item) {
        val bottomSheet = EditItemBottomSheet()
        val bundle = Bundle().apply {
            putInt("photo", item.photoResource)
            putString("name", item.name)
            putInt("quantity", item.quantity)
            putLong("expiryDate", item.expiryDate)
            putString("category", item.categoryId)
        }
        bottomSheet.arguments = bundle

        bottomSheet.onItemUpdatedListener = object : OnItemUpdatedListener {
            override fun onItemUpdated(name: String, quantity: Int, expiryDate: Long, category: String) {
                item.name = name
                item.quantity = quantity
                item.expiryDate = expiryDate
                CategoryManager.getCategories().find { it.displayName.equals(category, ignoreCase = true) }?.let {
                    item.categoryId = it.toString()
                }
                itemAdapter.notifyDataSetChanged()
            }
        }

        bottomSheet.show(parentFragmentManager, "EditItemBottomSheet")
    }

    private fun showItemOptionsDialog(item: Item): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle(item.name)
            .setItems(arrayOf("Consume", "Delete")) { _, which ->
                when (which) {
                    0 -> consumeItem(item)
                    1 -> deleteItem(item)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        return true
    }

    private fun consumeItem(item: Item) {
        if (item.quantity > 1) {
            item.quantity--
            item.status = ItemStatus.ACTIVE
            sharedItemViewModel.updateItem(item)
        } else {
            item.status = ItemStatus.CONSUMED
            sharedItemViewModel.updateItem(item)
            listOfItems.remove(item)
            filteredList.remove(item)
        }
        itemAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "${item.name} consumed!", Toast.LENGTH_SHORT).show()
        updateCategoryItemCounts()
    }

    private fun deleteItem(item: Item) {
        item.status = ItemStatus.DELETED
        sharedItemViewModel.updateItem(item)
        listOfItems.remove(item)
        filteredList.remove(item)
        itemAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "${item.name} deleted!", Toast.LENGTH_SHORT).show()
        updateCategoryItemCounts()
    }

    private fun updateCategoryCounts() {
        categoryList.forEach { it.itemCount = 0 }
        listOfItems.forEach { item ->
            categoryList.find { it.id == item.categoryId }?.incrementItemCount()
        }
        categoryAdapter.notifyDataSetChanged()
    }
}
