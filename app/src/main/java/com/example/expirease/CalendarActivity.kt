package com.example.expirease

import android.app.Activity
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CalendarActivity : Activity() {
    lateinit var calendarView : CalendarView;
    lateinit var calendar : Calendar;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendarr)
        calendarView = findViewById(R.id.calendar)
        calendar = Calendar.getInstance()

        calendarView.date = calendar.timeInMillis
//        setDate(2023,3,19)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(this, "Selected Date: $date", Toast.LENGTH_SHORT).show()
        }
    }

//    fun setDate(year: Int, month: Int, day: Int) {
//        calendar.set(Calendar.YEAR, year)
//        calendar.set(Calendar.MONTH, month-1)
//        calendar.set(Calendar.DAY_OF_MONTH, day)
//        calendarView.date = calendar.timeInMillis
//    }
}