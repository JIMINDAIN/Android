package com.example.mentalnote.ui

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mentalnote.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

val savedEmotions = mutableStateMapOf<LocalDate, String>(
    LocalDate.of(2025, 7, 1) to "😊",
    LocalDate.of(2025, 7, 2) to "😢",
    LocalDate.of(2025, 7, 3) to "😡",
    LocalDate.of(2025, 7, 4) to "😴",
    LocalDate.of(2025, 7, 5) to "😎",
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthTab() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val calendarViewState = remember { mutableStateOf<CalendarView?>(null) }
    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    var selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    Column{
        TopAppBar(
            title = {
                Text(
                    text = "${currentMonth.year}. ${currentMonth.monthValue}",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 17.sp)
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    currentMonth = currentMonth.minusMonths(1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp), contentDescription = "이전 달")
                }
            },
            actions = {
                IconButton(onClick = {
                    currentMonth = currentMonth.plusMonths(1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp), contentDescription = "다음 달")
                }
            }
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            //modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            for (day in daysOfWeek) {
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            //modifier = Modifier.padding(vertical = 16.dp)
        )

        AndroidView(
            factory = { ctx ->
                val calendarView = CalendarView(ctx).apply {
                    dayViewResource = R.layout.day_item
                    //pagedScroll = true
                }

                calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                    override fun create(view: View): DayViewContainer {
                        return DayViewContainer(view)
                    }

                    override fun bind(container: DayViewContainer, data: CalendarDay) {

                        container.day = data
                        container.textView.text = data.date.dayOfMonth.toString()
                        container.emojiView.text = savedEmotions[data.date] ?: ""


                        container.view.setOnClickListener {
                            println("선택한 날짜: ${data.date}")
                            selectedDate.value = data.date
                        }
                    }
                }

                calendarView.monthScrollListener = { month ->
                    currentMonth = month.yearMonth
                }

                val today = YearMonth.now()
                calendarView.setup(
                    startMonth = today.minusMonths(12),
                    endMonth = today.plusMonths(12),
                    firstDayOfWeek = DayOfWeek.SUNDAY
                )
                calendarView.scrollToMonth(today)

                calendarViewState.value = calendarView
                calendarView
            },
            modifier = Modifier
        )

        LaunchedEffect(currentMonth) {
            calendarViewState.value?.post {
                calendarViewState.value?.scrollToMonth(currentMonth)
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            //modifier = Modifier.padding(horizontal = 16.dp)
        )





    }
}
