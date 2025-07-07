package com.example.mentalnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.MonthTab
import com.example.mentalnote.ui.Tab2Screen
import com.example.mentalnote.ui.WeekTab
import com.example.mentalnote.ui.theme.MentalNoteTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

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
    val nanumFont1 = FontFamily(Font(R.font.dunggeunmo))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = { /* 필요시 아이콘 추가 */ },
                            label = {
                                Text(
                                    text = title,
                                    fontFamily = nanumFont1,
                                    fontSize = 15.sp
                                )},
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
                    1 -> Tab2Screen() // 혹은 GalleryTab(dayRecords = dayRecords)
                    2 -> MonthTab()
                }
            }
        }
    }
}
