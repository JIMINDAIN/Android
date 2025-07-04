package com.example.mentalnote

import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.mentalnote.ui.theme.MentalNoteTheme

data class DayRecord(
    val day: String,
    val emoji: String,
    val summary: String,
    val imageUri: Uri? = null,
    val imageBitmap: androidx.compose.ui.graphics.ImageBitmap? = null
)

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

    // 저장된 주간 데이터
    var dayRecords by remember { mutableStateOf(listOf<DayRecord>()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { /* 아이콘 생략 */ },
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
                0 -> WeekTab(dayRecords = dayRecords, onSave = { record ->
                    // 저장 로직: 기존 기록 업데이트 또는 추가
                    dayRecords = dayRecords.toMutableList().also { list ->
                        val idx = list.indexOfFirst { it.day == record.day }
                        if (idx >= 0) list[idx] = record else list.add(record)
                    }
                })
                1 -> GalleryTab(dayRecords = dayRecords)
                2 -> Text("Month 탭은 아직 구현 전", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun WeekTab(dayRecords: List<DayRecord>, onSave: (DayRecord) -> Unit) {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    var selectedDay by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        days.forEach { day ->
            val record = dayRecords.find { it.day == day }
            WeekRow(day = day, record = record, onClick = { selectedDay = day })
            Divider()
        }
    }

    if (selectedDay != null) {
        val record = dayRecords.find { it.day == selectedDay }
        DayDetailDialog(
            day = selectedDay!!,
            initialRecord = record,
            onDismiss = { selectedDay = null },
            onSave = { emoji, summary, imageUri, imageBitmap ->
                onSave(DayRecord(selectedDay!!, emoji, summary, imageUri, imageBitmap))
                selectedDay = null
            }
        )
    }
}

@Composable
fun WeekRow(day: String, record: DayRecord?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = record?.emoji ?: "😃",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = "${day}요일",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = record?.summary ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun DayDetailDialog(
    day: String,
    initialRecord: DayRecord?,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?, androidx.compose.ui.graphics.ImageBitmap?) -> Unit
) {
    var summary by remember { mutableStateOf(initialRecord?.summary ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialRecord?.emoji ?: "😃") }
    var imageUri by remember { mutableStateOf<Uri?>(initialRecord?.imageUri) }
    var cameraBitmap by remember { mutableStateOf(initialRecord?.imageBitmap) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            cameraBitmap = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            cameraBitmap = bitmap.asImageBitmap()
            imageUri = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${day}요일 기록") },
        text = {
            Column {
                TextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = { Text("한 줄 요약을 입력하세요") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "오늘의 기분 선택:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("😃", "🥲", "😡").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            modifier = Modifier
                                .clickable { selectedEmoji = emoji }
                                .padding(4.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (selectedEmoji == emoji) Color.Blue else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("사진 선택")
                    }
                    Button(onClick = { cameraLauncher.launch(null) }) {
                        Text("사진 촬영")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (cameraBitmap != null) {
                    Image(
                        bitmap = cameraBitmap!!,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    )
                } else if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(selectedEmoji, summary, imageUri, cameraBitmap)
            }) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun GalleryTab(dayRecords: List<DayRecord>) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Gallery 탭은 아직 구현 중입니다.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MentalNoteTheme {
        MainScreen()
    }
}
