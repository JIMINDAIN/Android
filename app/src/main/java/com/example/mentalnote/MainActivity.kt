package com.example.mentalnote

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.WeekTab
import com.example.mentalnote.ui.GalleryTab
import com.example.mentalnote.ui.MonthTab
import com.example.mentalnote.ui.theme.MentalNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalNoteTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Week", "Gallery", "Month")

    var dayRecords by remember { mutableStateOf(listOf<DayRecord>()) }
    val backgroundColor = Color(0xFFEAFDF9)

    Box(
        modifier = Modifier
            .fillMaxSize()   // 화면 전체 채우기
            .background(backgroundColor)  // 배경색 지정
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = { /* 필요시 아이콘 추가 */ },
                            label = { Text(title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                color = Color.Transparent
            ) {
                when (selectedTab) {
                    0 -> WeekTab(
                        dayRecords = dayRecords,
                        onSave = { record ->
                            dayRecords = dayRecords.toMutableList().also { list ->
                                val idx = list.indexOfFirst { it.date == record.date }
                                if (idx >= 0) list[idx] = record else list.add(record)
                            }
                        }
                    )

                    1 -> GalleryTab(dayRecords = dayRecords)
                    2 -> MonthTab()
                }
            }
        }
    }
}
