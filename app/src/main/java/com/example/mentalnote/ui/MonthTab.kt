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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mentalnote.R
import com.example.mentalnote.model.DayRecord
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

val nanumFont1 = FontFamily(Font(R.font.gangwon_bold))
val nanumFont2 = FontFamily(Font(R.font.gangwon_light))

val testDayRecords = mutableStateMapOf<LocalDate, DayRecord>(
    LocalDate.of(2025, 7, 1) to DayRecord(
        date = "2025-07-01",
        emoji = "😊",
        summary = "기분 좋았던 날",
        detail = "하늘이 맑고 기분 좋은 산책을 했음"
    ),
    LocalDate.of(2025, 7, 2) to DayRecord(
        date = "2025-07-02",
        emoji = "😢",
        summary = "조금 슬펐던 날",
        detail = "비가 와서 나가지 못함"
    ),
    LocalDate.of(2025, 7, 3) to DayRecord(
        date = "2025-07-03",
        emoji = "😡",
        summary = "짜증났던 날",
        detail = "버스 놓치고 지각함"
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthTab() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val calendarViewState = remember { mutableStateOf<CalendarView?>(null) }
    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    Column{
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "${currentMonth.year}. ${currentMonth.monthValue}",
                    color = Color.DarkGray,
                    fontFamily = nanumFont1,
                    //textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 17.sp),
                    //modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
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
                    fontFamily = nanumFont1,
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
                        container.emojiView.text = testDayRecords[data.date] ?.emoji ?: ""


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
            modifier = Modifier.padding(horizontal = 3.dp)
        )


        selectedDate.value?.let { date ->
            val record = testDayRecords[date]

            if(record == null){
                Text(
                    text = "기록이 없습니다.",
                    fontFamily = nanumFont1,
                    modifier = Modifier.padding(40.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray,
                    fontSize = 20.sp
                )
            } else{
                Column(
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        // 왼쪽: 이모지
                        Text(
                            text = record.emoji,
                            fontSize = 40.sp, // 이모지 크게
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        // 오른쪽: summary
                        Text(
                            text = record.summary,
                            fontFamily = nanumFont1,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    /*HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFEEEEEE),
                        //modifier = Modifier.padding(vertical = 16.dp)
                    )*/

                    Text(
                        text = record.detail,
                        fontFamily = nanumFont2,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 25.dp)
                    )

                }

            }
        }




    }
}
