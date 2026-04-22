package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.AttendanceResponse
import com.example.myapplication.data.model.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel : ViewModel() {
    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> get() = _calendarDays

    private val _currentMonthName = MutableLiveData<String>()
    val currentMonthName: LiveData<String> get() = _currentMonthName

    private val _presentCount = MutableLiveData<Int>()
    val presentCount: LiveData<Int> get() = _presentCount

    private val _absentCount = MutableLiveData<Int>()
    val absentCount: LiveData<Int> get() = _absentCount

    private val _weekendCount = MutableLiveData<Int>()
    val weekendCount: LiveData<Int> get() = _weekendCount

    private val _averageWorkingHours = MutableLiveData<String>()
    val averageWorkingHours: LiveData<String> get() = _averageWorkingHours

    private val _selectedAttendance = MutableLiveData<AttendanceResponse?>()
    val selectedAttendance: LiveData<AttendanceResponse?> get() = _selectedAttendance

    private var attendanceHistory: List<AttendanceResponse> = emptyList()
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    fun generateCalendar(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        _currentMonthName.value = monthName

        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay(0, "none", false))
        }
        for (i in 1..daysInMonth) {
            days.add(CalendarDay(i, "none", true))
        }
        _calendarDays.value = days
        
        if (attendanceHistory.isNotEmpty()) {
            setAttendanceData(attendanceHistory)
        }
    }

    fun onDateSelected(day: Int) {
        if (day == 0) return
        val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, day)
        val record = attendanceHistory.find { 
            val apiDate = it.date?.trim() ?: ""
            apiDate == dateStr || apiDate.startsWith(dateStr)
        }
        _selectedAttendance.value = record
    }

    fun setAttendanceData(history: List<AttendanceResponse>?) {
        if (history == null) return
        attendanceHistory = history

        val currentDays = _calendarDays.value ?: return
        var present = 0
        var absent = 0
        var weekend = 0

        val updatedDays = currentDays.map { day ->
            if (day.date == 0) return@map day
            
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, day.date)
            
            // Flexible matching for dates that might have time components or slightly different formats
            val record = history.find { 
                val apiDate = it.date?.trim() ?: ""
                apiDate == dateStr || apiDate.startsWith(dateStr)
            }

            if (record != null) {
                when {
                    record.status?.contains("PRESENT", ignoreCase = true) == true -> {
                        present++
                        day.copy(status = "present")
                    }
                    record.status?.contains("ABSENT", ignoreCase = true) == true -> {
                        absent++
                        day.copy(status = "absent")
                    }
                    record.status?.contains("WEEKEND", ignoreCase = true) == true -> {
                        weekend++
                        day.copy(status = "weekend")
                    }
                    else -> day.copy(status = "none")
                }
            } else {
                day
            }
        }

        _calendarDays.value = updatedDays
        _presentCount.value = present
        _absentCount.value = absent
        _weekendCount.value = weekend
    }
}
