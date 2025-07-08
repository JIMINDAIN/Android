package com.example.mentalnote.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.mentalnote.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.day_text)
    val emojiView: ImageView = view.findViewById(R.id.day_emoji)
    lateinit var day: CalendarDay
}