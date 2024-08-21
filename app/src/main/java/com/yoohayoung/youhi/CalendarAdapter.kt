package com.yoohayoung.youhi


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CalendarDay(val day: String, val event: String)

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onItemClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day.day
        holder.eventText.text = day.event

        // 이벤트가 있는 날에만 텍스트를 표시합니다.
        holder.eventText.visibility = if (day.event.isNotEmpty()) View.VISIBLE else View.INVISIBLE

        // 아이템 클릭 시 선택된 날짜를 전달합니다.
        holder.itemView.setOnClickListener {
            onItemClick(day)
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateData(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val eventText: TextView = view.findViewById(R.id.eventText)
    }
}