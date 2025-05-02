package com.example.expirease.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
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
import com.example.expirease.R
import com.example.expirease.helper.OnItemUpdatedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditItemBottomSheet : BottomSheetDialogFragment() {
    var onItemUpdatedListener: OnItemUpdatedListener? = null
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
        val itemName = view.findViewById<EditText>(R.id.item_name)
        val itemQuantity = view.findViewById<EditText>(R.id.edit_item_quantity)
        val itemExpiryDate = view.findViewById<EditText>(R.id.item_expiryDate)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinner_category)
//        val itemCategory = view.findViewById<EditText>(R.id.item_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnCalendar = view.findViewById<ImageButton>(R.id.btn_show_calendar)
        val btnIncrease = view.findViewById<Button>(R.id.btn_increase)
        val btnDecrease = view.findViewById<Button>(R.id.btn_decrease)

        val calendar = Calendar.getInstance()

        val categoryOptions = arrayOf("Dairy", "Meat", "Vegetable", "Fruit", "Beverage", "Others")
        spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions)


        // Retrieve data from arguments
        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.img_placeholder_product))
            itemName.setText(it.getString("name", "Unknown Item"))
            itemQuantity.setText(it.getInt("quantity", 0).toString())

            val expiryMillis = it.getLong("expiryDate", System.currentTimeMillis())  // Get expiry date
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiryMillis))
            itemExpiryDate.setText(formattedDate)
            calendar.timeInMillis = expiryMillis

            val category = it.getString("category", "others")
            val index = categoryOptions.indexOfFirst { it.equals(category, ignoreCase = true) }
            if (index >= 0) spinnerCategory.setSelection(index)
        }

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

        btnCalendar.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the EditText with selected date
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                itemExpiryDate.setText(formatted)

                // Save the date back to calendar if needed
                calendar.set(selectedYear, selectedMonth, selectedDay)

            }, year, month, day)

            datePicker.show()
        }


        btnSave.setOnClickListener {
            val updatedName = itemName.text.toString()
            val updatedQuantity = itemQuantity.text.toString().toIntOrNull() ?: 1


            val updatedExpiry = calendar.timeInMillis
            val updatedCategory = spinnerCategory.selectedItem.toString()

            onItemUpdatedListener?.onItemUpdated(updatedName, updatedQuantity, updatedExpiry, updatedCategory)

            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

    }

//    private fun updateCategoryItemCounts() {
//        // Reset all category counts to 0
//        categoryList.forEach { it.itemCount = 0 }
//
//        // Count items for each category
//        listOfItems.forEach { item ->
//            val category = categoryList.find { it.id.equals(item.categoryId, ignoreCase = true) }
//
//            if (category != null) {
//                category.itemCount++
//            } else {
//                // Handle invalid category
//                println("CategoryWarning: Item '${item.name}' has an invalid category '${item.categoryId}'")
//            }
//        }
//
//        // Update the adapter to reflect changes
//        categoryAdapter.notifyDataSetChanged()
//    }

}