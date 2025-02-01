package com.yoohayoung.youhi.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.yoohayoung.youhi.R
import com.yoohayoung.youhi.databinding.ActivityCreateEventBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class CreateEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var selectedDate :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_event)


        selectedDate = intent.getStringExtra("selectedDate").toString()
        Log.d("selectedDate","$selectedDate")

        binding.addEventButton.setOnClickListener {
            addEvent()
        }
    }

    private fun addEvent() {
        val eventTitle = binding.ETEventTitle.text.toString()
        val CB_eventIsPrivate = binding.CBEventIsPrivate.isChecked

        val eventKey = FBRef.eventsRef.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.

        FBRef.eventsRef
            .child(selectedDate)
            .child(eventKey)
            .setValue(EventModel(selectedDate, eventTitle, CB_eventIsPrivate, FBAuth.getUid()))

        finish()
    }
}