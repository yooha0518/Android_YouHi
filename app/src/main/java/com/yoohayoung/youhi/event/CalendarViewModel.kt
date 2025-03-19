package com.yoohayoung.youhi.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.CalendarDay
import com.yoohayoung.youhi.EventModel
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CalendarViewModel : ViewModel() {
    val calendar: Calendar = Calendar.getInstance()

    private val _eventsList = MutableLiveData<List<EventModel>>() //리스트에 표시되는 데이터
    val eventsList get() = _eventsList

    private val _friendsSet = MutableLiveData<Set<String>>()

    private val _events = MutableLiveData<Map<String, List<EventModel>>>() //캘린더에 표시되는 데이터 (하루당 최대 3개)
    val events: LiveData<Map<String, List<EventModel>>> get() = _events

    private val _selectedDate = MutableLiveData<Date?>()
    val selectedDate: LiveData<Date?> get() = _selectedDate

    private val _monthYearText = MutableLiveData<String>()
    val monthYearText: LiveData<String> get() = _monthYearText

    init {
        updateMonthYearText()
    }

    fun loadFriendsAndEvents(currentUserId: String) {
        val friendsSet = mutableSetOf<String>()
        FBRef.userRef.child(currentUserId).child("friends")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val friendUid = snapshot.key ?: continue
                            friendsSet.add(friendUid)
                        }
                        _friendsSet.postValue(friendsSet)
                    }
                    loadEventsFromDatabase()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("CalendarViewModel", "Failed to load friends", databaseError.toException())
                }
            })
    }

    fun loadEventsFromDatabase() {
        FBRef.eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val eventsMap = mutableMapOf<String, MutableList<EventModel>>()
                val currentUserUid = FBAuth.getUid()  // 현재 사용자 UID

                // friendsSet에서 실제 데이터 가져오기
                val friends = _friendsSet.value ?: emptySet()  // friendsSet 값 가져오기, 없으면 빈 Set

                for (dateSnapshot in dataSnapshot.children) {
                    val date = dateSnapshot.key
                    val eventList = mutableListOf<EventModel>()

                    for (eventSnapshot in dateSnapshot.children) {
                        val event = eventSnapshot.getValue(EventModel::class.java)
                        event?.let {
                            // Private 이벤트는 본인만 볼 수 있도록 필터링
                            if (event.private) {
                                if (currentUserUid == event.uid) {
                                    eventList.add(it)
                                }
                            } else {
                                // Public 이벤트는 친구만 볼 수 있음
                                if (friends.contains(event.uid) || currentUserUid == event.uid) {
                                    eventList.add(it)
                                }
                            }
                        }
                    }

                    // 날짜별 이벤트 목록에 필터링된 이벤트 추가
                    if (date != null && eventList.isNotEmpty()) {
                        eventsMap[date] = eventList
                    }
                }

                // 필터링된 이벤트 목록을 LiveData에 업데이트
                _events.postValue(eventsMap)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CalendarViewModel", "Failed to load events", databaseError.toException())
            }
        })
    }


    fun loadDayEvents(dateStr: String) {
        val currentUserUid = FBAuth.getUid()  // 현재 사용자 UID

        // friendsSet에서 실제 데이터 가져오기
        val friends = _friendsSet.value ?: emptySet()  // friendsSet 값 가져오기, 없으면 빈 Set

        FBRef.eventsRef.child(dateStr).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val eventList = mutableListOf<EventModel>()
                dataSnapshot.children.forEach { snapshot ->
                    val event = snapshot.getValue(EventModel::class.java)
                    event?.let {
                        event?.let {
                            // Private 이벤트는 본인만 볼 수 있도록 필터링
                            if (event.private) {
                                if (currentUserUid == event.uid) {
                                    eventList.add(it)
                                }
                            } else {
                                // Public 이벤트는 친구만 볼 수 있음
                                if (friends.contains(event.uid) || currentUserUid == event.uid) {
                                    eventList.add(it)
                                }
                            }
                        }
                    }
                }
                _eventsList.postValue(eventList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("CalendarViewModel", "loadDayEvents:onCancelled")
            }
        })
    }

    fun updateSelectedDate(day: CalendarDay) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        tempCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        tempCalendar.set(Calendar.DAY_OF_MONTH, day.day.toInt())

        _selectedDate.value = tempCalendar.time  // postValue가 아니라 value를 사용하여 즉시 업데이트
    }

    fun updateMonthYearText() {
        // SimpleDateFormat을 사용하여 월-연도 포맷으로 날짜 설정
        val formattedDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        _monthYearText.value = formattedDate
    }


    fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
    }

    fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
    }

    fun clearEventsList() {
        _eventsList.value = emptyList()
    }
}
