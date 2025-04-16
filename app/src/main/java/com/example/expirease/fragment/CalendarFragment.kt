package com.example.expirease.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helper.ItemRecyclerViewAdapter
import com.example.expirease.manager.SharedItemViewModel
import com.kizitonwose.calendar.view.*
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


class CalendarFragment : Fragment() {
    private val sharedItemViewModel: SharedItemViewModel by activityViewModels()

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemRecyclerViewAdapter

    private val allItems = mutableListOf<Item>()
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        recyclerView = view.findViewById(R.id.itemsRecyclerView)
        val monthTextView = view.findViewById<TextView>(R.id.monthTextView)
        val prevButton = view.findViewById<ImageButton>(R.id.previousMonthButton)
        val nextButton = view.findViewById<ImageButton>(R.id.nextMonthButton)

        adapter = ItemRecyclerViewAdapter(mutableListOf()) {}
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        allItems.addAll(
            listOf(
                Item("Milk", 2, LocalDate.now().toEpochDay(), Category.DAIRY),
                Item("Bread", 1, LocalDate.now().plusDays(1).toEpochDay(), Category.BAKERY),
                Item("Cheese", 3, LocalDate.now().toEpochDay(), Category.DAIRY),
                Item("Yogurt", 4, LocalDate.now().minusDays(1).toEpochDay(), Category.DAIRY),
                Item("Butter", 1, LocalDate.now().plusDays(3).toEpochDay(), Category.DAIRY)
            )
        )


        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = DayOfWeek.SUNDAY

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        filterItemsForDate(selectedDate)

        calendarView.monthScrollListener = { month ->
            val monthName = month.yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
            monthTextView.text = "$monthName ${month.yearMonth.year}"
        }

        prevButton.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.minusMonths(1))
            }
        }

        nextButton.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.plusMonths(1))
            }
        }

        // ✅ Correct way to inflate calendar day layout
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view)
            }


            override fun bind(container: DayViewContainer, data: CalendarDay) {
                Log.d("CalendarDebug", "Binding day: ${data.date}")
                container.dayText.text = data.date.dayOfMonth.toString()
                container.dayText.setTextSize(20f)
                container.dayText.setTypeface(null, Typeface.BOLD)

                val today = LocalDate.now()
                if (data.position == DayPosition.MonthDate) {
                    container.dayText.setTextColor(Color.BLACK)
                    container.dayText.setTypeface(null, if (data.date == today) Typeface.BOLD else Typeface.NORMAL)

                    container.view.setOnClickListener {
                        selectedDate = data.date
                        filterItemsForDate(data.date)
                        calendarView.notifyCalendarChanged()
                    }
                } else {
                    container.dayText.setTextColor(Color.GRAY)
                    container.view.setOnClickListener(null)
                }

                // Highlight selected day
                if (data.date == selectedDate) {
                    container.dayText.setBackgroundResource(R.drawable.bg_selected_day)
                } else {
                    container.dayText.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    private fun filterItemsForDate(date: LocalDate) {
        val filtered = sharedItemViewModel.getItemsForDate(date)
        adapter.updateData(filtered.toMutableList())
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val dayText: TextView = view.findViewById(R.id.calendarDayText)
        val container: FrameLayout = view.findViewById(R.id.dayContainer)

    }
}
