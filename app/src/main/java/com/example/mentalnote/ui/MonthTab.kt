package com.example.mentalnote.ui

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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


val nanumFont1 = FontFamily(Font(R.font.dunggeunmo))
val nanumFont2 = FontFamily(Font(R.font.gangwon_light))


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthTab(dayRecords : List<DayRecord>) {
    val context = LocalContext.current
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val calendarViewState = remember { mutableStateOf<CalendarView?>(null) }
    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    //var dayRecords by remember {mutableStateOf<List<DayRecord>>(emptyList()) }
    val dayRecordMap : Map<LocalDate, DayRecord> = dayRecords.associateBy {LocalDate.parse(it.date)}

    Column{
        AppHeader()

        CenterAlignedTopAppBar(
            //modifier = Modifier.height(50.dp),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFFEAFDF9),
            ),
            title = {
                Card(
                    shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFDF0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // 그림자 강조
                    modifier = Modifier
                        .width(200.dp)
                        .height(35.dp)
                        .align(Alignment.CenterHorizontally)
                        .border(0.5.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(16.dp))
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontSize = 15.sp)) { append("💖 ") } // 작은 하트
                                withStyle(style = SpanStyle(fontSize = 20.sp)) { append("${currentMonth.year}. ${currentMonth.monthValue} ") }
                                withStyle(style = SpanStyle(fontSize = 15.sp)) { append("💖") } // 작은 하트
                            },color = Color.Black,
                            fontFamily = nanumFont1,
                            //textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                            //modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                }

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


        /*HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            //modifier = Modifier.padding(bottom = 16.dp)
        )*/


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ){
            Card(
                shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // 그림자 강조
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(16.dp))
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ){
                        for (day in daysOfWeek) {
                            Text(
                                text = day,
                                fontFamily = nanumFont1,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
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
                                    dayRecordMap[data.date]?.emojiResID?.let {
                                        container.emojiView.setImageResource(it)
                                    }

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
                }
            }
        }

        LaunchedEffect(currentMonth) {
            calendarViewState.value?.post {
                calendarViewState.value?.scrollToMonth(currentMonth)
            }
        }

        /*HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 3.dp)
        )*/


        selectedDate.value?.let { date ->
            val record = dayRecordMap[date]

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ){
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFDF0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // 그림자 강조
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(16.dp))
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            // 왼쪽: 이모지
                            Image(
                                painter = painterResource(id = record.emojiResID ?: R.drawable.emoji_happy),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // 오른쪽: summary
                            Text(
                                text = record.summary,
                                fontFamily = nanumFont1,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    /*HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFEEEEEE),
                        //modifier = Modifier.padding(vertical = 16.dp)
                    )*/

                    Card(
                        shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5FFFA)
                        ), // 요일별 배경색
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // 그림자 강조
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .heightIn(min = 120.dp)
                            .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(16.dp))
                    ){
                        Text(
                            text = record.detail,
                            fontFamily = nanumFont1,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 15.dp, start = 25.dp, end = 25.dp)
                        )
                    }


                }

            }
        }




    }
}
