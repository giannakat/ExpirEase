package com.example.expirease.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helper.OnItemUpdatedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditItemBottomSheet : BottomSheetDialogFragment() {
    var onItemUpdatedListener: OnItemUpdatedListener? = null
    var itemNameKey: String? = null // Using item name as Firebase key

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_item, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemPhoto = view.findViewById<ImageView>(R.id.item_photo)
        val itemName = view.findViewById<EditText>(R.id.item_name2)
        val itemQuantity = view.findViewById<EditText>(R.id.edit_item_quantity2)
        val itemExpiryDate = view.findViewById<EditText>(R.id.item_expiryDate2)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinner_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnCalendar = view.findViewById<ImageButton>(R.id.btn_show_calendar)
        val btnIncrease = view.findViewById<Button>(R.id.btn_increase)
        val btnDecrease = view.findViewById<Button>(R.id.btn_decrease)
        val btnEditName = view.findViewById<ImageView>(R.id.editName)
        val calendar = Calendar.getInstance()

        val categoryOptions = arrayOf("Dairy", "Meat", "Vegetables", "Fruits", "Beverages", "Others")
        spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions)

        // Retrieve data from arguments
        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.img_placeholder_product))
            itemName.setText(it.getString("name", "Unknown Item"))
            itemQuantity.setText(it.getInt("quantity", 0).toString())

            val expiryMillis = it.getLong("expiryDate", System.currentTimeMillis())
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiryMillis))
            itemExpiryDate.setText(formattedDate)
            calendar.timeInMillis = expiryMillis

            val category = it.getString("category", "others")
            val index = categoryOptions.indexOfFirst { it.equals(category, ignoreCase = true) }
            if (index >= 0) spinnerCategory.setSelection(index)

            itemNameKey = it.getString("name") // Use name as Firebase key
        }

        btnEditName.setOnClickListener{
            itemName.isFocusableInTouchMode = true
            itemName.isFocusable = true
            itemName.isCursorVisible = true
            itemName.requestFocus()
            itemName.selectAll()
        }
        // Quantity update buttons
        btnIncrease.setOnClickListener {
            val current = itemQuantity.text.toString().toIntOrNull() ?: 0
            itemQuantity.setText((current + 1).toString())
        }

        btnDecrease.setOnClickListener {
            val current = itemQuantity.text.toString().toIntOrNull() ?: 0
            if (current > 1) {
                itemQuantity.setText((current - 1).toString())
            }
        }

        // Date picker
        btnCalendar.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                itemExpiryDate.setText(formatted)
                calendar.set(selectedYear, selectedMonth, selectedDay)
            }, year, month, day)

            datePicker.show()
        }

        // Save button logic
        btnSave.setOnClickListener {
            val updatedName = itemName.text.toString().trim()
            val updatedQuantity = itemQuantity.text.toString().toIntOrNull() ?: 0
            val updatedExpiry = calendar.timeInMillis
            val updatedCategoryName = spinnerCategory.selectedItem.toString()

            // Validate input
            if (updatedName.isEmpty() || updatedQuantity <= 0 || updatedCategoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determine the category ID and background color based on the selected category name
            val updatedCategoryId = updatedCategoryName.lowercase()  // Use the name as the ID
            val updatedCategoryBackgroundColor = when (updatedCategoryName.lowercase()) {
                "fruits" -> parseColor("#DFFAD6")  // example color
                "vegetables" -> parseColor("#E0F0FD")
                "dairy" -> parseColor("#ECEEFB")
                "meat" -> parseColor("#FFEEE5")
                "beverages" -> parseColor("#FFF0CC")
                else -> parseColor("#FFEED6")  // default color for "others"
            }

            // Create updatedCategory with the additional parameters
            val updatedCategory = Category(
                id = updatedCategoryId,
                displayName = updatedCategoryName,
                backgroundColor = updatedCategoryBackgroundColor
            )

            val updatedPhotoRes = getImageForCategory(updatedCategory)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("items")

            // Search item by original name passed as argument
            val originalName = arguments?.getString("name") ?: return@setOnClickListener

            ref.orderByChild("name").equalTo(originalName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val itemSnapshot = snapshot.children.first()
                        val itemKey = itemSnapshot.key!!

                        // Update fields in the database
                        ref.child(itemKey).child("name").setValue(updatedName)
                        ref.child(itemKey).child("quantity").setValue(updatedQuantity)
                        ref.child(itemKey).child("expiryDate").setValue(updatedExpiry)
                        ref.child(itemKey).child("categoryId").setValue(updatedCategoryName)
                        ref.child(itemKey).child("photoResource").setValue(updatedPhotoRes)

                        // Update the ViewModel with the new values
                        val updatedItem = Item(
                            name = updatedName,
                            quantity = updatedQuantity,
                            expiryDate = updatedExpiry,
                            categoryId = updatedCategoryName,
                            photoResource = updatedPhotoRes
                        )
                        (activity as? HomeFragment)?.sharedItemViewModel?.updateItemInViewModel(updatedItem)

                        // Update the UI elements in the fragment (such as the category and photo)
                        (activity as? HomeFragment)?.apply {
                            // Update the UI with the new item data
                            sharedItemViewModel.updateItemInViewModel(updatedItem)

                            // Ensure the spinner is updated to reflect the new category
                            spinnerCategory.setSelection(categoryOptions.indexOf(updatedCategoryName))

                            // Update the image for the item based on the selected category
                            itemPhoto.setImageResource(updatedPhotoRes)
                        }

                        onItemUpdatedListener?.onItemUpdated(
                            updatedName,
                            updatedQuantity,
                            updatedExpiry,
                            updatedCategoryName
                        )
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Item not found for update", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to access database", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Cancel button (dismiss without saving)
        btnCancel.setOnClickListener {
            dismiss()
        }
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
}
