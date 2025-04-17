package com.example.expirease.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helper.CategoryRecyclerViewAdapter
import com.example.expirease.helper.ItemRecyclerViewAdapter
import com.example.expirease.helper.OnItemUpdatedListener
import com.example.expirease.viewmodel.SharedItemViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.fragment.app.activityViewModels
import com.example.expirease.data.ItemStatus


class HomeFragment : Fragment(){
    private val sharedItemViewModel: SharedItemViewModel by activityViewModels()

    lateinit var itemAdapter : ItemRecyclerViewAdapter
    lateinit var filteredList: MutableList<Item>
    lateinit var listOfItems : MutableList<Item>

    lateinit var searchView: SearchView
    val categoryList = Category.values().toMutableList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listOfItems = mutableListOf()
        filteredList = listOfItems.toMutableList()

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // for searching
       // filteredList = listOfItems.toMutableList() // initialize filtered list to the current list

        setupRecyclerView(view)
        setupSearchView(view)
        setupAddButton(view)
        setupCategoryRecyclerView(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If you want to observe LiveData later
        sharedItemViewModel.allItems.observe(viewLifecycleOwner) { updatedList ->
            val activeItems = updatedList.filter { it.status == ItemStatus.ACTIVE || it.status == ItemStatus.EXPIRED }
            val sortedItems = activeItems.sortedBy { it.expiryDate }

            listOfItems.clear()
            listOfItems.addAll(activeItems)

            filteredList.clear()
            filteredList.addAll(sortedItems)

            itemAdapter.notifyDataSetChanged()
            updateUI(sortedItems)

        //            // Do something with the updated list, like update UI
//            filteredList.clear()
//            filteredList.addAll(updatedList)
//            itemAdapter.notifyDataSetChanged()
//            updateUI(updatedList)
        }
    }

    //extracted long function
    private fun setupRecyclerView(view: View) {
        val itemRecyclerView = view.findViewById<RecyclerView>(R.id.item_recyclerview)
        itemRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemAdapter = ItemRecyclerViewAdapter(
            filteredList,
            onClick = { item -> openEditItemBottomSheet(item) },
            onLongClick = { item -> showItemOptionsDialog(item) })

        itemRecyclerView.adapter = itemAdapter
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
            itemAdapter.notifyDataSetChanged()
        }
    }


    fun addItem(name: String, quantity: Int, expiryDate: Long, selectedCategory: Category, img: Int){
        val newItem = Item(name, quantity, ItemStatus.ACTIVE, expiryDate, selectedCategory, img)
        val currentTime = System.currentTimeMillis()
        val status = if (expiryDate < currentTime) ItemStatus.EXPIRED else ItemStatus.ACTIVE
        newItem.status = status
        //listOfItems.add(newItem)
        //filteredList.add(newItem)
        //itemAdapter.notifyItemInserted(filteredList.size - 1)

        sharedItemViewModel.addItem(newItem)
    }

    private fun setupSearchView(view:View) {
        searchView = view.findViewById<SearchView>(R.id.search_bar)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return true
            }
        })
    }

    private fun setupAddButton(view : View){
        val button_add = view.findViewById<ImageView>(R.id.add_button)
        button_add.setOnClickListener {
            showAddItemDialog()
            Toast.makeText(requireContext(), "button add clicked", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCategoryRecyclerView(view:View){
        val categoryRecyclerView = view.findViewById<RecyclerView>(R.id.category_recyclerview)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val categoryAdapter = CategoryRecyclerViewAdapter(categoryList){category ->
            Toast.makeText(requireContext(), "Clicked category: ${category.displayName}", Toast.LENGTH_LONG).show()

            filterItemsByCategory(category)
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
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)
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

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryList.map { it.displayName })
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

            if(!name.isNullOrEmpty() && selectedCategory != null ) {

                addItem(name, quantity, selectedExpiryDate, selectedCategory, R.drawable.img_product_banana)  // Add new item
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    fun isExpired(expiryDate: Long): Boolean{
        val today = System.currentTimeMillis()
        return expiryDate < today
    }

    fun isExpiringSoon(expiryDate: Long): Boolean {
        val today = System.currentTimeMillis()
        val threeDaysLater = today + (3 * 24 * 60 * 60 * 1000)
        return expiryDate in today..threeDaysLater
    }

//    private fun addItem(
//        name: String,
//        quantity: Int,
//        expiryDate: Long,
//        selectedCategory: Category,
//        img: Int
//    ) {
//        val newItem = Item(name, quantity, expiryDate, selectedCategory, img)
//
//        val app = requireActivity().application as MyApplication
//        app.listOfItems.add(newItem)
//
//        listOfItems.add(newItem)
//        filteredList.add(newItem)
//        itemAdapter.notifyItemInserted(filteredList.size - 1)
//    }

    private fun filterItemsByCategory(category: Category) {
        filteredList.clear()
        if (category == Category.OTHER) {
            filteredList.addAll(listOfItems)
        } else {
            filteredList.addAll(listOfItems.filter { it.category == category })
        }
        itemAdapter.notifyDataSetChanged()
    }

    private fun openEditItemBottomSheet(item: Item){

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
                // update your list and adapter here
                item.name = name
                item.quantity = quantity
                item.expiryDate = expiryDate
                item.category = Category.valueOf(category.uppercase())

                itemAdapter.notifyDataSetChanged()
            }
        }

        bottomSheet.show(parentFragmentManager, "EditItemBottomSheet")
    }

    private fun showItemOptionsDialog(item : Item): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle(item.name)
            .setItems(arrayOf("Consume", "Delete")) { dialog, which ->
                when (which) {
                    0 -> {
                        // Consume
                        if (item.quantity > 1) {
                            item.quantity-- // Decrease quantity
                            item.status = ItemStatus.CONSUMED // Set status as consumed
//                            itemAdapter.notifyDataSetChanged()

                            // Update item in ViewModel
                            sharedItemViewModel.updateItem(item)

                            // Remove from filteredList and listOfItems if not ACTIVE anymore
                            if (item.status != ItemStatus.ACTIVE) {
                                listOfItems.remove(item)
                                filteredList.remove(item)
                                itemAdapter.notifyDataSetChanged()
                            }
                            Toast.makeText(requireContext(), "${item.name} consumed!", Toast.LENGTH_SHORT).show()
                        } else {
                            // If only one left, set status as consumed
                            item.status = ItemStatus.CONSUMED // Set status as consumed
                            itemAdapter.notifyDataSetChanged()
                            Toast.makeText(requireContext(), "${item.name} consumed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        // Delete
                        item.status = ItemStatus.DELETED // Set status as deleted
//                        itemAdapter.notifyDataSetChanged()
                        sharedItemViewModel.updateItem(item)

                        listOfItems.remove(item)
                        filteredList.remove(item)
                        itemAdapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "${item.name} marked as deleted!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        return true
    }

//    private fun fetchItemsFromFirebase() {
//        val firebaseDatabase = FirebaseDatabase.getInstance()
//        val currentUser = FirebaseAuth.getInstance().currentUser
//
//        if (currentUser != null) {
//            val userId = currentUser.uid
//            val databaseReference: DatabaseReference = firebaseDatabase
//                    .getReference("Users")
//                    .child(userId)
//                    .child("items")
//
//            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    listOfItems.clear()
//                    filteredList.clear()
//
//                    for (itemSnapshot in snapshot.children) {
//                        val item = itemSnapshot.getValue(Item::class.java)
//                        if (item != null) {
//                            listOfItems.add(item)
//                        }
//                    }
//
//                    filteredList.addAll(listOfItems)
//                    itemAdapter.notifyDataSetChanged()
//
//                    Log.d("FirebaseItems", "Loaded ${listOfItems.size} items")
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("FirebaseError", "Error fetching items: ${error.message}")
//                }
//            })
//        } else {
//            Log.e("AuthError", "User not authenticated")
//        }
//    }

}

