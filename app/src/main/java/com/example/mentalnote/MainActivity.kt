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

// 각 요일별 기록을 담는 데이터 클래스
// 이미지가 Uri 형태일 수도, Bitmap 형태일 수도 있으므로 둘 다 저장 가능하도록 함
data class DayRecord(
    val day: String,                           // 요일 (월, 화, 수 등)
    val emoji: String,                         // 선택한 이모지
    val summary: String,                       // 한줄 요약 텍스트
    val imageUri: Uri? = null,                 // 갤러리 등에서 선택한 이미지의 Uri (null 가능)
    val imageBitmap: androidx.compose.ui.graphics.ImageBitmap? = null // 카메라로 찍은 이미지 (null 가능)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalNoteTheme {
                MainScreen() // 앱의 메인 화면 컴포저블 호출
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 현재 선택된 탭 인덱스 상태
    val tabs = listOf("Week", "Gallery", "Month")   // 탭 이름 리스트

    // 전체 주간 데이터 리스트 상태 (DayRecord 목록)
    var dayRecords by remember { mutableStateOf(listOf<DayRecord>()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                // 탭 바 생성, 클릭 시 selectedTab 업데이트
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
        // 탭별 화면 표시, innerPadding 적용
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                // Week 탭: 요일별 기록 및 편집 가능 (저장 콜백 포함)
                0 -> WeekTab(dayRecords = dayRecords, onSave = { record ->
                    // 기록 저장: 기존 기록 있으면 업데이트, 없으면 새로 추가
                    dayRecords = dayRecords.toMutableList().also { list ->
                        val idx = list.indexOfFirst { it.day == record.day }
                        if (idx >= 0) list[idx] = record else list.add(record)
                    }
                })
                // Gallery 탭: 현재는 비어 있음. 나중에 사진 보기 기능 구현 예정
                1 -> GalleryTab(dayRecords = dayRecords)
                // Month 탭: 아직 미구현, 임시 텍스트 출력
                2 -> Text("Month 탭은 아직 구현 전", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun WeekTab(dayRecords: List<DayRecord>, onSave: (DayRecord) -> Unit) {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    var selectedDay by remember { mutableStateOf<String?>(null) } // 선택된 요일 상태

    Column(modifier = Modifier.padding(16.dp)) {
        // 각 요일별 행을 생성
        days.forEach { day ->
            val record = dayRecords.find { it.day == day } // 해당 요일의 기록 조회
            WeekRow(day = day, record = record, onClick = { selectedDay = day }) // 클릭 시 편집 대화상자 열기
            Divider()
        }
    }

    // 요일 선택 시 기록 편집 다이얼로그 표시
    if (selectedDay != null) {
        val record = dayRecords.find { it.day == selectedDay }
        DayDetailDialog(
            day = selectedDay!!,
            initialRecord = record,
            onDismiss = { selectedDay = null }, // 취소 시 선택 초기화
            onSave = { emoji, summary, imageUri, imageBitmap ->
                // 저장 버튼 클릭 시 호출, 새로운 기록 전달
                onSave(DayRecord(selectedDay!!, emoji, summary, imageUri, imageBitmap))
                selectedDay = null // 다이얼로그 닫기
            }
        )
    }
}

@Composable
fun WeekRow(day: String, record: DayRecord?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // 클릭 시 편집 대화상자 호출
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // 이모지 표시, 기본값 😃
        Text(
            text = record?.emoji ?: "😃",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            // 요일 텍스트
            Text(
                text = "${day}요일",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            // 한줄 요약 텍스트, 없으면 빈 문자열
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
    // 다이얼로그 내 입력 상태 관리
    var summary by remember { mutableStateOf(initialRecord?.summary ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialRecord?.emoji ?: "😃") }
    var imageUri by remember { mutableStateOf<Uri?>(initialRecord?.imageUri) }
    var cameraBitmap by remember { mutableStateOf(initialRecord?.imageBitmap) }

    val context = LocalContext.current

    // 갤러리에서 이미지 선택할 때 사용하는 런처
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri    // 선택한 이미지 Uri 저장
            cameraBitmap = null // 카메라 이미지 초기화
        }
    }

    // 카메라로 사진 촬영할 때 사용하는 런처
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            cameraBitmap = bitmap.asImageBitmap() // 촬영한 비트맵 저장
            imageUri = null  // Uri 초기화
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${day}요일 기록") },
        text = {
            Column {
                // 한 줄 요약 입력 필드
                TextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = { Text("한 줄 요약을 입력하세요") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "오늘의 기분 선택:", style = MaterialTheme.typography.bodyMedium)
                // 이모지 선택 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("😃", "🥲", "😡").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            modifier = Modifier
                                .clickable { selectedEmoji = emoji } // 클릭 시 선택 이모지 변경
                                .padding(4.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (selectedEmoji == emoji) Color.Blue else Color.Transparent, // 선택 시 파란색 테두리
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 사진 선택 및 촬영 버튼
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
                // 선택된 사진(카메라/갤러리) 미리보기 표시
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
                    // Uri로부터 비트맵을 로드해서 이미지로 표시
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
                // 저장 버튼 클릭 시 onSave 콜백 호출
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

// Gallery 탭 화면
// 현재는 비어있고, 나중에 사진 목록을 보여주는 기능 개발 예정
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
