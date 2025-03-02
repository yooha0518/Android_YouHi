package com.yoohayoung.youhi.event

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoohayoung.youhi.EventModel
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.GlideOptions
import de.hdodenhof.circleimageview.CircleImageView

class DayEventAdapter(
    private val events:List<EventModel>, private val eventActionListener: EventActionListener
) : RecyclerView.Adapter<DayEventAdapter.EventViewHolder>() {

    interface EventActionListener{
        fun onEventLongClick(eventModel: EventModel)
    }
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val CIV_profile: CircleImageView = view.findViewById(R.id.CIV_profile)
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

        // 프로필 이미지를 Glide를 사용하여 로드
        Glide.with(holder.CIV_profile.context)
            .load("http://youhi.tplinkdns.com:4000/${event.uid}.jpg")
            .apply(GlideOptions.profileOptions)
            .into(holder.CIV_profile)

        // 닉네임을 비동기적으로 가져와서 설정
        FBAuth.getNickName(event.uid) { nickName ->
            holder.TV_nickName.text = nickName
        }

        val myUid = FBAuth.getUid()
        val writerUid = event.uid

        Log.d("DayEvnetAdapter", "event: $event")

        holder.itemView.setOnLongClickListener{
            if (myUid == writerUid) {
                eventActionListener.onEventLongClick(event)
                Log.d("DayEventAdapter", "롱클릭 이벤트 실행됨: $event")
            }
            true
        }

    }

}