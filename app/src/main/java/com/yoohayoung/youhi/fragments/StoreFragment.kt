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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.board.BoardModel
import com.yoohayoung.youhi.event.EventModel
import com.yoohayoung.youhi.friend.Friend
import com.yoohayoung.youhi.friend.FriendAdapter
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import java.text.ParseException
import java.util.Date

class StoreFragment : Fragment() {

    private lateinit var binding: FragmentStoreBinding
    private lateinit var calendarAdapter: CalendarAdapter

    private val calendar = Calendar.getInstance()
    private val currentMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var selectedDate: Date? = null
    private val events = mutableMapOf<String, MutableList<EventModel>>() // 날짜를 키로 하고 이벤트 리스트를 값으로 하는 맵
    private val friendsSet = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_store, container, false)

        setupCalendar()
        loadFriendsAndEventLoad()

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
            selectedDate = getDateForDay(day) //클릭 이벤트 리스너
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

    private fun getDaysInMonthArray(): List<CalendarDay> { //이벤트를 DB에서 가져와서 추가
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
            daysInMonth.add(CalendarDay("", mutableListOf())) // 빈 날짜
        }

        // 현재 달의 날짜만 추가
        for (day in 1..lastDayOfMonth) { //날짜를 추가하면서 이벤트 추가
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val date = tempCalendar.time
            val formattedDay = SimpleDateFormat("d", Locale.getDefault()).format(date)
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val eventList = events[fullDate] ?: mutableListOf()


            daysInMonth.add(CalendarDay(formattedDay, eventList))
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
        val eventTitle = binding.ETEventTitle.text.toString()
        val CB_eventIsPrivate = binding.CBEventIsPrivate.isChecked
        selectedDate?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(it)

            val eventKey = FBRef.eventsRef.push().key.toString() // 데이터가 생성되기 전에 키값을 먼저 받을 수 있다.

            FBRef.eventsRef
                .child(dateStr.toString())
                .child(eventKey)
                .setValue(EventModel(dateStr.toString(), eventTitle, CB_eventIsPrivate, FBAuth.getUid()))

            updateCalendar()
            binding.ETEventTitle.text.clear()
            Toast.makeText(context, "Event added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFriendsAndEventLoad() {
        val currentUserId = FBAuth.getUid()

        FBRef.userRef.child(currentUserId).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    friendsSet.clear() // 기존 친구 목록 초기화

                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val friendUid = snapshot.key ?: continue
                            friendsSet.add(friendUid) // 친구 목록에 추가
                        }

                        Log.d("friendList", "$friendsSet")
                        // 친구 목록 로드 완료 후 이벤트 로드
                    }else{
                        Log.d("CalendarFragment", "No friends found")
                    }

                    loadEventsFromDatabase()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FriendFragment1", "Failed to load friends", databaseError.toException())
                }
            })
    }

    private fun loadEventsFromDatabase() {
        FBRef.eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                events.clear() // 기존 이벤트 데이터를 초기화

                for (dateSnapshot in dataSnapshot.children) {
                    val date = dateSnapshot.key // 날짜를 가져옴
                    val eventList = mutableListOf<EventModel>()

                    // 각 날짜 하위의 이벤트들 가져오기
                    for (eventSnapshot in dateSnapshot.children) {
                        val event = eventSnapshot.getValue(EventModel::class.java)
                        if (event != null) {
                            val currentUserUid = FBAuth.getUid()
                            Log.d("isPrivate","${event.private}")
                            if (event.private) {
                                // Private 이벤트는 본인만 볼 수 있음
                                if (currentUserUid == event.uid) {
                                    eventList.add(event)
                                }
                            } else {
                                // Public 이벤트는 친구만 볼 수 있음
                                if (friendsSet.contains(event.uid) || currentUserUid == event.uid) {
                                    eventList.add(event)
                                }
                            }
                        } else {
                            Log.e("StoreFragment", "Failed to parse event: ${eventSnapshot.value}")
                        }
                    }

                    if (date != null && eventList.isNotEmpty()) {
                        events[date] = eventList
                    }
                }

                updateCalendar() // 이벤트를 불러온 후 캘린더 업데이트
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("StoreFragment", "Failed to load events", databaseError.toException())
            }
        })
    }



    private fun updateCalendar() {
        calendarAdapter.updateData(getDaysInMonthArray())
        updateMonthYearText()
    }

    private fun updateSelectedDate(day: CalendarDay) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${day.day.padStart(2, '0')}"


        Log.d("updateSelectedDate","click")
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
    }
}
