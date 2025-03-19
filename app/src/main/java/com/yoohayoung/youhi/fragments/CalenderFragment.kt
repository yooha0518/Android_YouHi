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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.yoohayoung.youhi.CalendarAdapter
import com.yoohayoung.youhi.CalendarDay
import com.yoohayoung.youhi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.recyclerview.widget.GridLayoutManager
import com.yoohayoung.youhi.EventModel
import com.yoohayoung.youhi.databinding.DialogEditEventBinding
import com.yoohayoung.youhi.event.CreateEventActivity
import com.yoohayoung.youhi.databinding.FragmentCalenderBinding
import com.yoohayoung.youhi.event.CalendarViewModel
import com.yoohayoung.youhi.event.DayEventAdapter
import com.yoohayoung.youhi.utils.FBAuth
import com.yoohayoung.youhi.utils.FBRef
import androidx.lifecycle.Observer

class CalendarFragment : Fragment(), CalendarAdapter.DayActionListener, DayEventAdapter.EventActionListener {

    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var dayEventAdapter: DayEventAdapter
    private val viewModel: CalendarViewModel by viewModels() // ViewModel 연결

    private val currentMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var dateStr: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val currentUserId = FBAuth.getUid() // 현재 사용자 ID
        viewModel.loadFriendsAndEvents(currentUserId)

        val daysInMonth = getDaysInMonthArray(emptyMap())
        calendarAdapter = CalendarAdapter(daysInMonth, viewModel.calendar, this)
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)  // 7일로 그리드 레이아웃 설정
        binding.calendarRecyclerView.adapter = calendarAdapter

        dayEventAdapter = DayEventAdapter(viewModel.eventsList.value ?: mutableListOf(), this)
        binding.RVDayEventList.layoutManager = GridLayoutManager(context, 1)
        binding.RVDayEventList.adapter = dayEventAdapter

        viewModel.events.observe(viewLifecycleOwner) { events ->
            // 데이터가 변경될 때마다 캘린더 업데이트
            updateCalendar(events)
        }

        viewModel.eventsList.observe(viewLifecycleOwner) { eventsList ->
            dayEventAdapter.updateData(eventsList)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { selectedDate ->
            selectedDate?.let {
                dateStr = dateFormat.format(it)
                viewModel.loadDayEvents(dateStr)
            }
        }

        binding.addEventButton.setOnClickListener {
            val intent = Intent(context, CreateEventActivity::class.java)
            intent.putExtra("selectedDate", dateStr)
            startActivity(intent)
        }

        binding.previousMonthButton.setOnClickListener {
            viewModel.clearEventsList()
            viewModel.moveToPreviousMonth()
            updateCalendar()
            viewModel.loadFriendsAndEvents(currentUserId)
        }

        binding.nextMonthButton.setOnClickListener {
            viewModel.clearEventsList()
            viewModel.moveToNextMonth()
            updateCalendar()
            viewModel.loadFriendsAndEvents(currentUserId)
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


    private fun updateCalendar(events: Map<String, List<EventModel>> = emptyMap()) {
        // 캘린더 업데이트 로직
        val daysInMonth = getDaysInMonthArray(events)
        calendarAdapter.updateData(daysInMonth)
        viewModel.updateMonthYearText()
    }


    override fun onDayListClick(day: CalendarDay) {
        viewModel.updateSelectedDate(day)
    }

    override fun onEventLongClick(event: EventModel) {
        showEditEventAlertDialog(event)
    }

    private fun getDaysInMonthArray(events: Map<String, List<EventModel>>): List<CalendarDay> {
        val daysInMonth = mutableListOf<CalendarDay>()
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = viewModel.calendar.time
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val lastDayOfMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 빈 날짜 추가
        for (i in 0 until firstDayOfMonth) {
            daysInMonth.add(CalendarDay("", mutableListOf()))
        }

        // 날짜 추가
        for (day in 1..lastDayOfMonth) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val date = tempCalendar.time
            val formattedDay = SimpleDateFormat("d", Locale.getDefault()).format(date)
            val fullDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val eventList = events[fullDate] ?: mutableListOf()
            daysInMonth.add(CalendarDay(formattedDay, eventList))
        }

        return daysInMonth
    }

    private fun showEditEventAlertDialog(event: EventModel) {
        val binding = DialogEditEventBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        val editTextDate = binding.etEditDate
        val editTextTitle = binding.etEditTitle
        val switchPrivate = binding.switchPrivate
        val btnSave = binding.btnSave
        val btnCancel = binding.btnCancel
        val btnDelete = binding.btnDelete

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // binding 해제
    }
}