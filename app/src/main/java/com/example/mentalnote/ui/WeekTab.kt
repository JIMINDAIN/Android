package com.example.mentalnote.ui

import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import com.example.mentalnote.R
import com.google.accompanist.permissions.*
import android.content.Context
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import android.util.Log
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

//WeekTab을 구현하는 메인함수
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

    //매주 월요일마다 초기화
    //기존 데이터는 그대로 유지되고 WeekRecord 변수만 초기화
    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val lastResetDateStr = prefs[LAST_RESET_DATE_KEY]
        val lastResetDate = lastResetDateStr?.let { LocalDate.parse(it) }

        val existingRecords = loadDayRecords(context).toMutableList()

        if (today.dayOfWeek == DayOfWeek.MONDAY && lastResetDate != monday) {
            // 이번 주 날짜만 새로운 빈 DayRecord로 교체
            // 만약 오늘이 월요일이고, 저장된 마지막 초기화 날짜(lastResetDate)가 이번 주 월요일과 다르면
            // (= 월요일인데 초기화가 안된 경우)
            weekDateStrings.forEach { dateStr ->
                val idx = existingRecords.indexOfFirst { it.date == dateStr }
                if (idx != -1) {
                    existingRecords[idx] = DayRecord(date = dateStr)
                } else {
                    existingRecords.add(DayRecord(date = dateStr))
                }
            }

            weekRecords = existingRecords.filter { it.date in weekDateStrings }
            coroutineScope.launch {
                context.dataStore.edit { prefs ->
                    prefs[LAST_RESET_DATE_KEY] = monday.toString()
                }
                saveDayRecords(context, existingRecords)
            }
        } else {
            weekRecords = existingRecords.filter { it.date in weekDateStrings }
        }
    }

    //Column 안에서 7일치 날짜별로 WeekRow 컴포저블을 호출하여 날짜와 기록(있으면 요약 및 이모지)을 보여준다
    //각 WeekRow 클릭 시 해당 날짜(selectedDate)가 선택되어 상세 기록 작성 다이얼로그가 뜬다
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
//            .background(Color(0xffe3ffea))
    ) {
        weekDateStrings.forEach { dateStr ->
            val record = weekRecords.find { it.date == dateStr }
            WeekRow(date = dateStr, record = record, onEmojiClick = { selectedEmoji ->
                // 클릭 시 DayDetailDialog 열기
                selectedDate = dateStr
                // 나중에 selectedEmoji 값도 Dialog에 넘길 수 있음
            })

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (selectedDate != null) {
        val record = weekRecords.find { it.date == selectedDate }
        //DayDetailDialog 함수를 활용해 날마다 상세 정보를 입력한다
        DayDetailDialog(
            date = selectedDate!!,
            initialRecord = record,
            onDismiss = { selectedDate = null },
            onSave = { emojiResID, summary, detail, imageUri, imageBitmap ->
                val newRecord = DayRecord(
                    date = selectedDate!!,
                    emojiResID = emojiResID,
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
fun dayOfWeekToDrawableRes(dayOfWeek: DayOfWeek): Int {
    return when(dayOfWeek) {
        DayOfWeek.MONDAY -> R.drawable.monday
        DayOfWeek.TUESDAY -> R.drawable.tuesday
        DayOfWeek.WEDNESDAY -> R.drawable.wednesday
        DayOfWeek.THURSDAY -> R.drawable.thursday
        DayOfWeek.FRIDAY -> R.drawable.friday
        DayOfWeek.SATURDAY -> R.drawable.saturday
        DayOfWeek.SUNDAY -> R.drawable.sunday
    }
}
@Composable
fun WeekRow(
    date: String,
    record: DayRecord?,
    onEmojiClick: (emoji: String) -> Unit
) {
    val localDate = LocalDate.parse(date)
    val dayofWeek = localDate.dayOfWeek
    val backgroundColor = when (dayofWeek) {
        DayOfWeek.MONDAY -> Color(0xffffe3e3)
        DayOfWeek.TUESDAY -> Color(0xffffe7cb)
        DayOfWeek.WEDNESDAY -> Color(0xfffffecb)
        DayOfWeek.THURSDAY -> Color(0xffe1ffcb)
        DayOfWeek.FRIDAY -> Color(0xffcbfffc)
        DayOfWeek.SATURDAY -> Color(0xffffe1e1)
        DayOfWeek.SUNDAY -> Color(0xfffce1ff)
    }

    val dayImageRes = when (localDate.dayOfWeek) {
        DayOfWeek.MONDAY -> R.drawable.monday
        DayOfWeek.TUESDAY -> R.drawable.tuesday
        DayOfWeek.WEDNESDAY -> R.drawable.wednesday
        DayOfWeek.THURSDAY -> R.drawable.thursday
        DayOfWeek.FRIDAY -> R.drawable.friday
        DayOfWeek.SATURDAY -> R.drawable.saturday
        DayOfWeek.SUNDAY -> R.drawable.sunday
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = dayImageRes),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { rotationZ = -15f }
                .offset(x = -8.dp, y = -35.dp)
                .align(Alignment.TopStart)
                .zIndex(1f)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly // 대칭 배치
            ) {
                val emojis = listOf(
                    R.drawable.emoji_happy to "happy",
                    R.drawable.emoji_bored to "bored",
                    R.drawable.emoji_blue to "blue",
                    R.drawable.emoji_upset to "upset"
                )

                emojis.forEach { (resId, emojiName) ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = emojiName,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onEmojiClick(emojiName) }
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
    onSave: (Int?, String, String, Uri?, androidx.compose.ui.graphics.ImageBitmap?) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val emojiOptions = listOf(
        R.drawable.emoji_happy,
        R.drawable.emoji_blue,
        R.drawable.emoji_bored,
        R.drawable.emoji_upset
    )

    var summary by remember { mutableStateOf(initialRecord?.summary ?: "") }
    var detail by remember { mutableStateOf(initialRecord?.detail ?: "") }
    var selectedEmojiRes by remember { mutableStateOf(initialRecord?.emojiResID ?: null) }
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
        title = { Text("오늘은 어떤 일이 있었나요?") },
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
                Text(text = "Today's mood")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    emojiOptions.forEach{ resID ->
                        Image(
                            painter = painterResource(id = resID),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedEmojiRes = resID }
                                .border(
                                    width = 2.dp,
                                    color = if (resID == selectedEmojiRes) Color.Black else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
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
                onSave(selectedEmojiRes, summary, detail, imageUri, cameraBitmap)
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
