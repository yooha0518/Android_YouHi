package com.yoohayoung.youhi.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.yoohayoung.youhi.CalendarAdapter
import com.yoohayoung.youhi.CalendarDay
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.FragmentStoreBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.recyclerview.widget.GridLayoutManager
import java.text.ParseException
import java.util.Date

class StoreFragment : Fragment() {

    private lateinit var binding: FragmentStoreBinding
    private lateinit var calendarAdapter: CalendarAdapter

    private val calendar = Calendar.getInstance()
    private val currentMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var selectedDate: Date? = null
    private val events = mutableMapOf<String, String>() // 날짜를 키로 하고 이벤트를 값으로 하는 맵

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_store, container, false)

        setupCalendar()

        binding.addEventButton.setOnClickListener {
            addEvent()
        }

        binding.previousMonthButton.setOnClickListener {
            moveToPreviousMonth()
        }

        binding.nextMonthButton.setOnClickListener {
            moveToNextMonth()
        }

        binding.tipTap.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_storeFragment_to_friendFragment)
        }
        binding.bookmarkTap.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_storeFragment_to_bookmarkFragment)
        }
        binding.talkTap.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_storeFragment_to_talkFragment)
        }
        binding.homeTap.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_storeFragment_to_homeFragment)
        }

        return binding.root
    }

    private fun setupCalendar() {
        val daysInMonth = getDaysInMonthArray()
        calendarAdapter = CalendarAdapter(daysInMonth) { day ->
            selectedDate = getDateForDay(day)
            updateSelectedDate(day)
        }
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7) // 7 columns for the days of the week
        binding.calendarRecyclerView.adapter = calendarAdapter

        updateMonthYearText()
    }

    private fun updateMonthYearText() {
        binding.monthYearText.text = currentMonthYearFormat.format(calendar.time)
    }

    private fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateCalendar()
    }

    private fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateCalendar()
    }

    private fun getDaysInMonthArray(): List<CalendarDay> {
        val daysInMonth = mutableListOf<CalendarDay>()
        val tempCalendar = Calendar.getInstance()

        // 현재 달로 설정
        tempCalendar.time = calendar.time
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        // 현재 달의 첫 번째 날과 마지막 날 구하기
        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val lastDayOfMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 첫 주의 빈 날짜 추가
        for (i in 0 until firstDayOfMonth) {
            daysInMonth.add(CalendarDay("", "")) // 빈 날짜
        }

        // 현재 달의 날짜만 추가
        for (day in 1..lastDayOfMonth) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val date = tempCalendar.time
            val formattedDay = SimpleDateFormat("d", Locale.getDefault()).format(date)
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val event = events[fullDate] ?: ""
            daysInMonth.add(CalendarDay(formattedDay, event))
        }

        return daysInMonth
    }

    private fun getDateForDay(day: CalendarDay): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${day.day.padStart(2, '0')}"

        return try {
            dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    private fun addEvent() {
        val eventDescription = binding.ETEventTitle.text.toString()
        selectedDate?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(it)
            events[dateStr] = eventDescription
            updateCalendar()
            binding.ETEventTitle.text.clear()
            Toast.makeText(context, "Event added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCalendar() {
        calendarAdapter.updateData(getDaysInMonthArray())
        updateMonthYearText()
    }

    private fun updateSelectedDate(day: CalendarDay) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${day.day.padStart(2, '0')}"

        selectedDate = try {
            dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }

        binding.TVSelectedDate.text = if (selectedDate != null) {
            "Selected Date: $dateStr"
        } else {
            "Selected Date: Invalid date"
        }

        binding.TVSelectedDate.setText(events[dateStr])
    }
}
