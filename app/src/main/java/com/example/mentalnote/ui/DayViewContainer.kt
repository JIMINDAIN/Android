package com.example.mentalnote.ui

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import com.example.mentalnote.R

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.day_text)
    val emojiView: TextView = view.findViewById(R.id.day_emoji)
    lateinit var day: CalendarDay
}