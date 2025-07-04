package com.example.mentalnote.ui
import com.example.mentalnote.dataStore
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import com.example.mentalnote.model.DayRecord
import androidx.compose.foundation.verticalScroll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import java.time.LocalDate
import java.time.DayOfWeek



val DAY_RECORDS_KEY = stringPreferencesKey("day_records")

suspend fun saveDayRecords(context: Context, records: List<DayRecord>) {
    val json = Json.encodeToString(records)
    context.dataStore.edit { prefs ->
        prefs[DAY_RECORDS_KEY] = json
    }
}

suspend fun loadDayRecords(context: Context): List<DayRecord> {
    val prefs = context.dataStore.data.first()
    val json = prefs[DAY_RECORDS_KEY] ?: return listOf("월", "화", "수", "목", "금", "토", "일").map {
        DayRecord(it, emoji = "😃", summary = "", detail = "")
    }
    return try {
        Json.decodeFromString(json)
    } catch (e: Exception) {
        emptyList()
    }
}


@Composable
fun WeekTab(dayRecords: List<DayRecord>, onSave: (DayRecord) -> Unit) {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    var selectedDay by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // 초기화 관련 추가
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weekRecords by remember { mutableStateOf(dayRecords) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val lastResetDateStr = prefs[stringPreferencesKey("last_reset_date")]
        val today = LocalDate.now()
        val mondayOfThisWeek = today.with(DayOfWeek.MONDAY)
        val lastResetDate = lastResetDateStr?.let { LocalDate.parse(it) }

        if (today.dayOfWeek == DayOfWeek.MONDAY && lastResetDate != mondayOfThisWeek) {
            // 월요일이고, 이번 주에 아직 초기화 안했으면 초기화
            val newRecords = days.map { day ->
                DayRecord(day, emoji = "😃", summary = "", detail = "")
            }
            weekRecords = newRecords

            coroutineScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[stringPreferencesKey("last_reset_date")] = mondayOfThisWeek.toString()
                }
                saveDayRecords(context, newRecords) // 필요 시 영구저장
            }
        } else {
            // 기존 데이터 불러오기
            weekRecords = loadDayRecords(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        days.forEach { day ->
            val record = weekRecords.find { it.day == day }
            WeekRow(day = day, record = record, onClick = { selectedDay = day })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (selectedDay != null) {
        val record = weekRecords.find { it.day == selectedDay }
        DayDetailDialog(
            day = selectedDay!!,
            initialRecord = record,
            onDismiss = { selectedDay = null },
            onSave = { emoji, summary, detail, imageUri, imageBitmap ->
                val newRecord = DayRecord(selectedDay!!, emoji, summary, detail, imageUri, imageBitmap)
                weekRecords = weekRecords.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.day == selectedDay }
                    if (idx >= 0) list[idx] = newRecord else list.add(newRecord)
                }
                onSave(newRecord)
                coroutineScope.launch {
                    saveDayRecords(context, weekRecords)
                }
                selectedDay = null
            }
        )
    }
}




@Composable
fun WeekRow(day: String, record: DayRecord?, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (record == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day}요일",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }   else {
                Text(
                    text = record?.emoji ?: "😃",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )
                Column {
                    Text(
                        text = "${day}요일",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = record?.summary ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}


@Composable
fun DayDetailDialog(
    day: String,
    initialRecord: DayRecord?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Uri?, androidx.compose.ui.graphics.ImageBitmap?) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var summary by remember { mutableStateOf(initialRecord?.summary ?: "") }
    var detail by remember { mutableStateOf(initialRecord?.detail ?: "") }
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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = detail,
                    onValueChange = { detail = it },
                    placeholder = { Text("상세 기록을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 10
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
                onSave(selectedEmoji, summary, detail, imageUri, cameraBitmap)
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
