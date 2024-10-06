package com.yoohayoung.youhi.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth

class DayEventAdapter(
    private var events:List<EventModel>
) : RecyclerView.Adapter<DayEventAdapter.EventViewHolder>() {
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val TV_date: TextView = view.findViewById(R.id.TV_date)
        val TV_nickName: TextView = view.findViewById(R.id.TV_nickName)
        val TV_event: TextView = view.findViewById(R.id.TV_event)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_event, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.TV_date.text = event.date
        holder.TV_event.text = event.title

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(event.uid) { nickName ->
            holder.TV_nickName.text = nickName
        }

    }

}