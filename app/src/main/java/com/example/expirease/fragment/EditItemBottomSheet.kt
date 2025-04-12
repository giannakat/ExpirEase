package com.example.expirease.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
        val itemQuantity = view.findViewById<EditText>(R.id.item_quantity)
        val itemExpiryDate = view.findViewById<EditText>(R.id.item_expiryDate)
        val itemCategory = view.findViewById<EditText>(R.id.item_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnCalendar = view.findViewById<ImageButton>(R.id.btn_show_calendar)

        val calendar = Calendar.getInstance()

        // Retrieve data from arguments
        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.img_product_banana))
            itemName.setText(it.getString("name", "Unknown Item"))
            itemQuantity.setText(it.getInt("quantity", 0).toString())

            val expiryMillis = it.getLong("expiryDate", System.currentTimeMillis())  // Get expiry date
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiryMillis))
            itemExpiryDate.setText(formattedDate)
            calendar.timeInMillis = expiryMillis

            val category = it.getString("category", "others")
            itemCategory.setText(category)
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

            // Parse back date from EditText
           // val dateText = itemExpiryDate.text.toString().replace("Expiry Date: ", "")
            //val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
          //  val parsedDate = formatter.parse(dateText)
           // val updatedExpiry = parsedDate?.time ?: System.currentTimeMillis()
            val updatedExpiry = calendar.timeInMillis
            val updatedCategory = itemCategory.text.toString().ifBlank { "Others" }

            onItemUpdatedListener?.onItemUpdated(updatedName, updatedQuantity, updatedExpiry, updatedCategory)

            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

    }

}