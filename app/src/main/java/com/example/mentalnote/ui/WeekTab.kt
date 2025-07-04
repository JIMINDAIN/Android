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
val LAST_RESET_DATE_KEY = stringPreferencesKey("last_reset_date")

suspend fun saveDayRecords(context: Context, records: List<DayRecord>) {
    val json = Json.encodeToString(records)
    context.dataStore.edit { prefs ->
        prefs[DAY_RECORDS_KEY] = json
    }
}

suspend fun loadDayRecords(context: Context): List<DayRecord> {
    val prefs = context.dataStore.data.first()
    val json = prefs[DAY_RECORDS_KEY]
    return if (json == null) {
        emptyList()
    } else {
        try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Composable
fun WeekTab(dayRecords: List<DayRecord>, onSave: (DayRecord) -> Unit) {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    val weekDates = (0..6).map { monday.plusDays(it.toLong()) }
    val weekDateStrings = weekDates.map { it.toString() }

    var selectedDate by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weekRecords by remember { mutableStateOf(dayRecords) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val lastResetDateStr = prefs[LAST_RESET_DATE_KEY]
        val lastResetDate = lastResetDateStr?.let { LocalDate.parse(it) }

        if (today.dayOfWeek == DayOfWeek.MONDAY && lastResetDate != monday) {
            // 월요일이고 이번 주에 초기화 안 했다면 초기화
            val newRecords = weekDateStrings.map { dateStr ->
                DayRecord(date = dateStr)
            }
            weekRecords = newRecords

            coroutineScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[LAST_RESET_DATE_KEY] = monday.toString()
                }
                saveDayRecords(context, newRecords)
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
        weekDateStrings.forEach { dateStr ->
            val record = weekRecords.find { it.date == dateStr }
            WeekRow(date = dateStr, record = record, onClick = { selectedDate = dateStr })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (selectedDate != null) {
        val record = weekRecords.find { it.date == selectedDate }
        DayDetailDialog(
            date = selectedDate!!,
            initialRecord = record,
            onDismiss = { selectedDate = null },
            onSave = { emoji, summary, detail, imageUri, imageBitmap ->
                val newRecord = DayRecord(
                    date = selectedDate!!,
                    emoji = emoji,
                    summary = summary,
                    detail = detail,
                    imageUriString = imageUri?.toString(),
                    imageBitmap = imageBitmap
                )
                weekRecords = weekRecords.toMutableList().also { list ->
                    val idx = list.indexOfFirst { it.date == selectedDate }
                    if (idx >= 0) list[idx] = newRecord else list.add(newRecord)
                }
                onSave(newRecord)
                coroutineScope.launch {
                    saveDayRecords(context, weekRecords)
                }
                selectedDate = null
            }
        )
    }
}

@Composable
fun WeekRow(date: String, record: DayRecord?, onClick: () -> Unit) {
    val localDate = LocalDate.parse(date)
    val dayOfWeekKorean = when (localDate.dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

    val isEmptyRecord = record == null || record.summary.isEmpty()

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
        if (isEmptyRecord) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${dayOfWeekKorean}요일",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.emoji,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp),
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text(
                        text = "${dayOfWeekKorean}요일",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = record.summary,
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
    date: String,
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
        title = {
            val localDate = LocalDate.parse(date)
            val dayOfWeekKorean = when (localDate.dayOfWeek) {
                DayOfWeek.MONDAY -> "월"
                DayOfWeek.TUESDAY -> "화"
                DayOfWeek.WEDNESDAY -> "수"
                DayOfWeek.THURSDAY -> "목"
                DayOfWeek.FRIDAY -> "금"
                DayOfWeek.SATURDAY -> "토"
                DayOfWeek.SUNDAY -> "일"
            }
            Text(text = "${dayOfWeekKorean}요일 기록")
        },
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
                    modifier = Modifier.fillMaxWidth().height(120.dp),
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
