package com.yoohayoung.youhi

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.databinding.ItemCalendarDayBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val calendar: Calendar,
    private val dayActionListener:DayActionListener
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    interface DayActionListener{
        fun onDayListClick(day:CalendarDay)
    }

    // 선택된 날짜를 문자열로 저장
    private var selectedDate: String? = null
    private val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // 오늘 날짜

    class DayViewHolder(binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root) {
        val dayText: TextView = binding.dayText
        val ET_eventTitle1: TextView = binding.ETEventTitle1
        val ET_eventTitle2: TextView = binding.ETEventTitle2
        val ET_eventTitle3: TextView = binding.ETEventTitle3
        val LL_calender_day: LinearLayout = binding.LLCalenderDay
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day.day

        // 기본 배경색으로 리셋
        holder.LL_calender_day.setBackgroundColor(Color.parseColor("#FFFFFF"))

        // 텍스트 색상 초기화
        holder.dayText.setTextColor(Color.parseColor("#6E6E6E")) // 기본 텍스트 색상

        // 현재 날짜 문자열 구하기 (ex. "2024-10-05")
        val currentDateString = "${calendar.get(Calendar.YEAR)}-${(calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${day.day.padStart(2, '0')}"

        // 오늘 날짜일 경우 배경색을 다르게 설정
        if (currentDateString == todayDateString) {
            holder.LL_calender_day.setBackgroundColor(Color.parseColor("#FFF6C6"))
        }

        // 선택된 날짜와 현재 날짜가 일치하면 배경색 변경
        if (currentDateString == selectedDate) {
            holder.LL_calender_day.setBackgroundColor(Color.parseColor("#FCE6ED")) // 선택된 날짜의 배경색
            holder.dayText.setTextColor(Color.parseColor("#EB326F"))
        }

        // 이벤트가 있는지 확인하고, 최대 3개의 이벤트만 표시
        when (day.events.size) {
            0 -> {
                holder.ET_eventTitle1.visibility = View.INVISIBLE
                holder.ET_eventTitle2.visibility = View.INVISIBLE
                holder.ET_eventTitle3.visibility = View.INVISIBLE
            }
            1 -> {
                holder.ET_eventTitle1.text = day.events[0].title
                holder.ET_eventTitle1.visibility = View.VISIBLE
                holder.ET_eventTitle2.visibility = View.INVISIBLE
                holder.ET_eventTitle3.visibility = View.INVISIBLE
            }
            2 -> {
                holder.ET_eventTitle1.text = day.events[0].title
                holder.ET_eventTitle2.text = day.events[1].title
                holder.ET_eventTitle1.visibility = View.VISIBLE
                holder.ET_eventTitle2.visibility = View.VISIBLE
                holder.ET_eventTitle3.visibility = View.INVISIBLE
            }
            else -> {
                holder.ET_eventTitle1.text = day.events[0].title
                holder.ET_eventTitle2.text = day.events[1].title
                holder.ET_eventTitle3.text = day.events[2].title
                holder.ET_eventTitle1.visibility = View.VISIBLE
                holder.ET_eventTitle2.visibility = View.VISIBLE
                holder.ET_eventTitle3.visibility = View.VISIBLE
            }
        }

        // 빈 날짜는 클릭 불가능하게 설정
        if (day.day.isEmpty()) {
            holder.itemView.isEnabled = false
            holder.itemView.setOnClickListener(null)
        } else {
            holder.itemView.isEnabled = true
            holder.itemView.setOnClickListener {
                // 선택된 날짜를 업데이트
                selectedDate = currentDateString

                // 전체 아이템 새로고침
                notifyDataSetChanged()

                dayActionListener.onDayListClick(day)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateData(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    fun resetSelectedDate(){
        selectedDate = null
    }

}

