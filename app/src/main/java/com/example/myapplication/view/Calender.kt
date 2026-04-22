package com.example.myapplication.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.AttendanceViewModel
import com.example.myapplication.viewmodel.CalendarViewModel
import java.util.*

class Calender : AppCompatActivity() {
    
    private val viewModel: CalendarViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        NavigationUtils.setupBottomNavigation(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCalendar)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvMonthYear = findViewById<TextView>(R.id.tvMonthYear)

        val tvPresentCount = findViewById<TextView>(R.id.tvPresentCount)
        val tvAbsentCount = findViewById<TextView>(R.id.tvAbsentCount)
        val tvWeekendCount = findViewById<TextView>(R.id.tvWeekendCount)
        val tvAverageTime = findViewById<TextView>(R.id.tvAverageTime)

        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = GridLayoutManager(this, 7)

        // 1. Reset Title as requested
        tvTitle.text = "Attendance"

        // 2. Check Employee ID
        val empId = com.example.myapplication.MyApplication.sessionManager.fetchEmpIdEms().orEmpty()
        Log.d("CalenderActivity", "Fetching attendance for EMS empId: $empId")

        // 3. Initialize Calendar
        val cal = Calendar.getInstance()
        viewModel.generateCalendar(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        // 4. Observers
        viewModel.currentMonthName.observe(this) { tvMonthYear.text = it }
        viewModel.presentCount.observe(this) { tvPresentCount.text = "$it Days" }
        viewModel.absentCount.observe(this) { tvAbsentCount.text = "$it Days" }
        viewModel.weekendCount.observe(this) { tvWeekendCount.text = "$it Days" }

        viewModel.calendarDays.observe(this) { daysList ->
            recyclerView.adapter = CalendarAdapter(daysList) { day ->
                viewModel.onDateSelected(day)
            }
        }

        viewModel.selectedAttendance.observe(this) { record ->
            if (record != null) {
                showAttendanceDialog(record)
            } else {
                Toast.makeText(this, "No record found for this date", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Fetch Data
        if (empId.isNotEmpty()) {
            attendanceViewModel.fetchAttendanceHistory(empId)
        }

        attendanceViewModel.attendanceHistory.observe(this) { history ->
            Log.d("CalenderActivity", "Attendance history received: ${history.size} records")
            Toast.makeText(this, "Fetched ${history.size} records", Toast.LENGTH_SHORT).show()
            
            // Log the first few records to check date format
            history.take(3).forEach { 
                Log.d("CalenderActivity", "Record: Date=${it.date}, Status=${it.status}")
            }
            viewModel.setAttendanceData(history)
        }

        attendanceViewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty() && !error.contains("not found", ignoreCase = true)) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAttendanceDialog(record: com.example.myapplication.data.model.AttendanceResponse) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_attendance_details)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val tvDialogDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
        val tvDialogInTime = dialog.findViewById<TextView>(R.id.tvDialogInTime)
        val tvDialogOutTime = dialog.findViewById<TextView>(R.id.tvDialogOutTime)
        val tvDialogWorkingHours = dialog.findViewById<TextView>(R.id.tvDialogWorkingHours)
        val btnDialogClose = dialog.findViewById<Button>(R.id.btnDialogClose)

        tvDialogDate.text = "Date: ${record.date ?: "N/A"}"
        tvDialogInTime.text = "Check-in: ${record.inTime ?: "--:--"}"
        tvDialogOutTime.text = "Check-out: ${record.outTime ?: "--:--"}"
        tvDialogWorkingHours.text = "Total Hours: ${record.workingHour ?: "--:--"}"

        btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}