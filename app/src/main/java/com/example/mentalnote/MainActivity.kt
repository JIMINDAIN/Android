package com.example.mentalnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { /* 아이콘 넣을 수 있음, 지금은 생략 */ },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> WeekTab()
                1 -> Text("Gallery 탭은 아직 구현 전", modifier = Modifier.padding(16.dp))
                2 -> Text("Month 탭은 아직 구현 전", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun WeekTab() {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column {
        days.forEach { day ->
            val isSelected = day == selectedDay
            Text(
                text = day,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) Color.Blue else Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        selectedDay = day
                        showDialog = true
                    }
                    .padding(horizontal = 16.dp)
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "요일 선택") },
            text = { Text(text = "${selectedDay}요일을 선택했습니다.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MentalNoteTheme {
        MainScreen()
    }
}
