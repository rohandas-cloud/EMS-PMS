package com.example.myapplication.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.CalendarDay

class CalendarAdapter(
    private val daysList: List<CalendarDay>,
    private val onDateClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val dotStatus: View = view.findViewById(R.id.dotStatus)
        val llDayContainer: View = view.findViewById(R.id.llDayContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysList[position]
        val context = holder.itemView.context

        if (day.date == 0) {
            holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.tvDayNumber.text = day.date.toString()

            when (day.status) {
                "present" -> {
                    holder.dotStatus.visibility = View.VISIBLE
                    holder.dotStatus.setBackgroundResource(R.drawable.dot_present)
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Green
                }
                "absent" -> {
                    holder.dotStatus.visibility = View.VISIBLE
                    holder.dotStatus.setBackgroundResource(R.drawable.dot_absent)
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#F44336")) // Red
                }
                "weekend" -> {
                    holder.dotStatus.visibility = View.INVISIBLE
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#9E9E9E")) // Grey
                }
                else -> {
                    holder.dotStatus.visibility = View.INVISIBLE
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#000000")) // Black
                }
            }

            holder.itemView.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDateClick(day.date)
                }
            }
        }
    }

    override fun getItemCount(): Int = daysList.size
}
