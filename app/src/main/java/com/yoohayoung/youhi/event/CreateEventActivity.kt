package com.yoohayoung.youhi.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yoohayoung.youhi.EventModel
import com.yoohayoung.youhi.databinding.ActivityCreateEventBinding
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef

class CreateEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var selectedDate :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)


        selectedDate = intent.getStringExtra("selectedDate").toString()


        binding.addEventButton.setOnClickListener {
            addEvent()
        }
    }

    private fun addEvent() {
        val eventTitle = binding.ETEventTitle.text.toString()
        val CB_eventIsPrivate = binding.CBEventIsPrivate.isChecked

        val eventKey = FBRef.eventsRef.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.

        Log.d("selectedDate","$selectedDate")

        FBRef.eventsRef
            .child(selectedDate)
            .child(eventKey)
            .setValue(EventModel(eventKey, selectedDate, eventTitle, CB_eventIsPrivate, FBAuth.getUid()))

        finish()
    }
}