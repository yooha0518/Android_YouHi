package com.yoohayoung.youhi


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.event.EventModel

//data class CalendarDay(val day: String, val event: String)
data class CalendarDay(val day: String, val events: MutableList<EventModel>) // 이벤트 배열로 변경


class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onItemClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val ET_eventTitle1: TextView = view.findViewById(R.id.ET_eventTitle1)
        val ET_eventTitle2: TextView = view.findViewById(R.id.ET_eventTitle2)
        val ET_eventTitle3: TextView = view.findViewById(R.id.ET_eventTitle3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day.day

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
                onItemClick(day)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateData(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}
