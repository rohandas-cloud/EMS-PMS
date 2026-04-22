package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.util.NavigationUtils
import com.example.myapplication.viewmodel.AttendanceViewModel
import com.example.myapplication.viewmodel.LeaveViewModel
import com.example.myapplication.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private val leaveViewModel: LeaveViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        NavigationUtils.setupBottomNavigation(this)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvName = findViewById<TextView>(R.id.tvName)
        val cvPayslip = findViewById<CardView>(R.id.cvPayslip)
        val cvLeave = findViewById<CardView>(R.id.cvLeave)
        val cvAttendance = findViewById<CardView>(R.id.cvAttendance)
        val cvHolidays = findViewById<CardView>(R.id.cvHolidays)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        val tvInTimeSummary = findViewById<TextView>(R.id.tvInTimeSummary)
        val tvOutTimeSummary = findViewById<TextView>(R.id.tvOutTimeSummary)
        val tvWorkingHoursSummary = findViewById<TextView>(R.id.tvWorkingHoursSummary)
        val tvLeaveBalanceSummary = findViewById<TextView>(R.id.tvLeaveBalanceSummary)
        val tvLeaveRequestSummary = findViewById<TextView>(R.id.tvLeaveRequestSummary)

        // --- GREETING AND NAME LOGIC ---
        val userName = MyApplication.sessionManager.fetchUserName() ?: "Employee"
        tvName.text = userName
        tvGreeting.text = getGreetingMessage()

        // --- ATTENDANCE LOGIC ---
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        Log.d("SecondActivity", "Dashboard empId for Attendance: $empId")
        
        if (!empId.isNullOrBlank()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            attendanceViewModel.fetchDailyAttendance(empId, today)
        }

        attendanceViewModel.dailyAttendance.observe(this) { attendance ->
            tvInTimeSummary.text = attendance?.inTime ?: "--:--"
            tvOutTimeSummary.text = attendance?.outTime ?: "--:--"
            tvWorkingHoursSummary.text = attendance?.workingHour ?: "--:--"
        }

        attendanceViewModel.statusMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Make the "In Time" row clickable for Check-in/Out
        findViewById<View>(R.id.tvInTimeSummary).parent.let { 
            (it as? View)?.setOnClickListener {
                attendanceViewModel.markAttendance()
            }
        }

        // --- LEAVE LOGIC ---
        leaveViewModel.fetchLeaveBalance()
        leaveViewModel.leaveBalances.observe(this) { balance ->
            if (balance != null) {
                tvLeaveBalanceSummary.text = "${balance.casualLeave ?: 0} / ${balance.totalLeave ?: 0}"
            }
        }

        leaveViewModel.fetchLeaveHistory()
        leaveViewModel.leaveHistory.observe(this) { history ->
            val pendingCount = history?.count { it.status?.equals("Pending", ignoreCase = true) == true } ?: 0
            tvLeaveRequestSummary.text = if (pendingCount > 0) "$pendingCount Pending" else "No Pending Request"
        }

        ivProfile.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Logout") viewModel.logout()
                true
            }
            popup.show()
        }

        viewModel.logoutResult.observe(this) { result ->
            if (result.isSuccess) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        cvPayslip.setOnClickListener { startActivity(Intent(this, PayslipActivity::class.java)) }
        cvLeave.setOnClickListener { startActivity(Intent(this, LeaveRequestActivity::class.java)) }
        cvAttendance.setOnClickListener { startActivity(Intent(this, Calender::class.java)) }
        cvHolidays.setOnClickListener { startActivity(Intent(this, HolidayActivity::class.java)) }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}