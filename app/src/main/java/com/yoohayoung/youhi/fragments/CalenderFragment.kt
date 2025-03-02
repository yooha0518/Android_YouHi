package com.yoohayoung.youhi.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.findNavController
import com.yoohayoung.youhi.CalendarAdapter
import com.yoohayoung.youhi.CalendarDay
import com.yoohayoung.youhi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoohayoung.youhi.EventModel
import com.yoohayoung.youhi.event.CreateEventActivity
import com.yoohayoung.youhi.databinding.FragmentCalenderBinding
import com.yoohayoung.youhi.event.DayEventAdapter
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import java.text.ParseException
import java.util.Date

class CalenderFragment : Fragment(),CalendarAdapter.DayActionListener, DayEventAdapter.EventActionListener {

    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var dayEventAdapter: DayEventAdapter
    val eventsList = mutableListOf<EventModel>()

    private val calendar = Calendar.getInstance()
    private val currentMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: Date? = null
    private var dateStr: String =""
    private val events = mutableMapOf<String, MutableList<EventModel>>() // 날짜를 키로 하고 이벤트 리스트를 값으로 하는 맵
    private val friendsSet = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)

        val daysInMonth = getDaysInMonthArray()

        calendarAdapter = CalendarAdapter(daysInMonth, calendar, this)
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)  // 7일로 그리드 레이아웃 설정
        binding.calendarRecyclerView.adapter = calendarAdapter

        dayEventAdapter = DayEventAdapter(eventsList, this)
        binding.RVDayEventList.layoutManager = GridLayoutManager(context, 1)
        binding.RVDayEventList.adapter = dayEventAdapter

        updateMonthYearText()
        loadFriendsAndEventLoad()

        binding.addEventButton.setOnClickListener {
            val intent = Intent(context, CreateEventActivity::class.java)
            intent.putExtra("selectedDate", dateStr)
            startActivity(intent)
        }

        binding.previousMonthButton.setOnClickListener {
            moveToPreviousMonth()
        }

        binding.nextMonthButton.setOnClickListener {
            moveToNextMonth()
        }

        binding.IVMenubarFriend.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_calenderFragment_to_friendFragment)
        }
        binding.IVMenubarLike.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_calenderFragment_to_likeFragment)
        }
        binding.IVMenubarBoard.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_calenderFragment_to_talkFragment)
        }
        binding.IVMenubarHome.setOnClickListener {
            Log.d("HomeFragment", "click")
            it.findNavController().navigate(R.id.action_calenderFragment_to_homeFragment)
        }

        return binding.root
    }

    override fun onDayListClick(day: CalendarDay) {
        selectedDate = getDateForDay(day) //클릭 이벤트 리스너
        updateSelectedDate(day)

        dateStr = dateFormat.format(selectedDate)

        loadDayEvents(dateStr)
    }

    override fun onEventLongClick(event: EventModel) {
        showEditEventAlertDialog(event)
    }

    private fun updateMonthYearText() {
        binding.TVMonthYearText.text = currentMonthYearFormat.format(calendar.time)
    }

    private fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateCalendar()
        calendarAdapter.resetSelectedDate()
        eventsList.clear()
        dayEventAdapter.notifyDataSetChanged()
    }

    private fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateCalendar()
        calendarAdapter.resetSelectedDate()
        eventsList.clear()
        dayEventAdapter.notifyDataSetChanged()
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

    private fun loadFriendsAndEventLoad() {
        val currentUserId = FBAuth.getUid()

        FBRef.userRef.child(currentUserId).child("friends")
            .addValueEventListener(object : ValueEventListener {
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
        FBRef.eventsRef.addValueEventListener(object : ValueEventListener {
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
                            Log.e("CalenderFragment", "Failed to parse event: ${eventSnapshot.value}")
                        }
                    }

                    if (date != null && eventList.isNotEmpty()) {
                        events[date] = eventList
                    }
                }

                updateCalendar() // 이벤트를 불러온 후 캘린더 업데이트
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CalenderFragment", "Failed to load events", databaseError.toException())
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

        selectedDate = try {
            dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }

    }

    private fun loadDayEvents(dateStr: String) {
        Log.d("선택한 날짜의 데이터", "${FBRef.eventsRef.child(dateStr)}")

        // 데이터 쿼리
        FBRef.eventsRef.child(dateStr).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                eventsList.clear()
                val currentUserUid = FBAuth.getUid()
                for (eventSnapshot in dataSnapshot.children) {
                    val event = eventSnapshot.getValue(EventModel::class.java)

                    event?.let {
                        if (event.private) {
                            // Private 이벤트는 본인만 볼 수 있음
                            if (currentUserUid == event.uid) {
                                eventsList.add(it)
                            }
                        } else {
                            // Public 이벤트는 친구만 볼 수 있음
                            if (friendsSet.contains(event.uid) || currentUserUid == event.uid) {
                                eventsList.add(it)
                            }
                        }
                    }
                }
                dayEventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("CalenderFragment", "loadDayEvents:onCancelled")
            }
        })

    }

    private fun showEditEventAlertDialog(event: EventModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_event, null)

        val editTextDate = dialogView.findViewById<EditText>(R.id.et_edit_date)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.et_edit_title)
        val switchPrivate = dialogView.findViewById<SwitchCompat>(R.id.switch_private)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)

        Log.d("showEditEventAlertDialog", "event: $event")

        // 기존 값 설정
        editTextDate.setText(event.date)
        editTextTitle.setText(event.title)
        switchPrivate.isChecked = event.private

        // 날짜 선택을 위한 DatePickerDialog 설정
        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                editTextDate.setText(formattedDate) // 선택한 날짜를 EditText에 설정
            }, year, month, day)

            datePickerDialog.show()
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener {
            val updatedDate = editTextDate.text.toString().trim()
            val updatedTitle = editTextTitle.text.toString().trim()
            val updatedPrivate = switchPrivate.isChecked

            if (updatedDate.isNotEmpty() && updatedTitle.isNotEmpty()) {
                val updatedEvent = event.copy(date = updatedDate, title = updatedTitle, private = updatedPrivate)
                // 데이터베이스 업데이트
                FBRef.eventsRef.child(event.date).child(event.eventId).removeValue()
                FBRef.eventsRef.child(updatedDate).child(event.eventId).setValue(updatedEvent)
                Toast.makeText(context, "이벤트 수정 완료", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "날짜와 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 삭제 버튼 클릭 리스너
        btnDelete.setOnClickListener {
            FBRef.eventsRef.child(event.date).child(event.eventId).removeValue()
            Toast.makeText(context, "이벤트 삭제 완료", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 취소 버튼 클릭 리스너
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }




}
