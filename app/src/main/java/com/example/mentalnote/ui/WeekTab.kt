package com.example.mentalnote.ui

import com.google.accompanist.permissions.*
import android.content.Context
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.example.mentalnote.dataStore
import com.example.mentalnote.model.DayRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter


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
            val newRecords = weekDateStrings.map { DayRecord(date = it) }
            weekRecords = newRecords
            coroutineScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[LAST_RESET_DATE_KEY] = monday.toString()
                }
                saveDayRecords(context, newRecords)
            }
        } else {
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
                coroutineScope.launch { saveDayRecords(context, weekRecords) }
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
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        if (isEmptyRecord) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("${dayOfWeekKorean}요일", fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(record?.emoji ?: "", fontSize = 40.sp, modifier = Modifier.padding(end = 16.dp))
                Column {
                    Text("${dayOfWeekKorean}요일", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(record?.summary ?: "", color = Color.DarkGray)
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var summary by remember { mutableStateOf(initialRecord?.summary ?: "") }
    var detail by remember { mutableStateOf(initialRecord?.detail ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialRecord?.emoji ?: "😃") }
    var imageUri by remember { mutableStateOf<Uri?>(initialRecord?.imageUri) }
    var cameraBitmap by remember { mutableStateOf(initialRecord?.imageBitmap) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    // 권한 상태 체크
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // 사진 촬영 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri.value != null) {
            imageUri = photoUri.value
            cameraBitmap = null
        }
    }

    // 갤러리 선택 런처 (기존 유지)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            cameraBitmap = null
        }
    }

    fun launchCamera() {
        if (hasCameraPermission) {
            // 권한 있으면 촬영 실행
            val file = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            photoUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            // 권한 없으면 요청
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("기록 작성") },
        text = {
            Column {
                // 1. 한 줄 요약 입력
                TextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = { Text("한 줄 요약") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 2. 상세 기록 입력
                TextField(
                    value = detail,
                    onValueChange = { detail = it },
                    placeholder = { Text("상세 기록") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 3. 이모지 선택
                Text(text = "오늘의 기분 선택:")
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

                // 사진 선택 & 촬영 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("사진 선택")
                    }
                    Button(onClick = { launchCamera() }) {
                        Text("사진 촬영")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 사진 미리보기
                imageUri?.let {
                    val painter = rememberAsyncImagePainter(model = it)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    )
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
