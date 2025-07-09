package com.example.mentalnote.ui


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import coil.compose.rememberAsyncImagePainter
import com.example.mentalnote.R
import com.example.mentalnote.dataStore
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.theme.CustomFontFamily
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

val json = Json {
    ignoreUnknownKeys = true
}

suspend fun saveDayRecords(context: Context, records: List<DayRecord>) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = Firebase.firestore

    records.forEach { record ->
        db.collection("users").document(userId)
            .collection("dayRecords").document(record.date)
            .set(record, SetOptions.merge())
            .await()
    }
}

suspend fun loadDayRecords(context: Context): List<DayRecord> {
    Log.d("loadDayRecords", "불러오기 시작")
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        Log.d("loadDayRecords", "User not logged in.")
        return emptyList()
    }

    val db = Firebase.firestore
    return try {
        val querySnapshot = db.collection("users").document(userId)
            .collection("dayRecords").get().await()
        querySnapshot.documents.mapNotNull { it.toObject(DayRecord::class.java) }
    } catch (e: Exception) {
        Log.e("loadDayRecords", "Error loading records", e)
        emptyList()
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
        val existingRecords = loadDayRecords(context).toMutableList()
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val weekDateStrings = weekDates.map { it.toString() }

        if (today.dayOfWeek == DayOfWeek.MONDAY) {
            // 이번 주 날짜만 새로운 빈 DayRecord로 교체
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
            //.padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        AppHeader()
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.padding(8.dp)){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f), // 세로 길이의 1/4 차지
                contentAlignment = Alignment.Center // 박스 내에서 텍스트 중앙 정렬
            ) {
                Text(
                    text = "오늘의 기분을 입력하세요!",

                    fontFamily = CustomFontFamily,
                    fontSize = 19.sp, // 폰트 크기
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    lineHeight = 30.sp // 행간 간격 높임
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            weekDateStrings.forEach { dateStr ->
                val record = weekRecords.find { it.date == dateStr }
                WeekRow(date = dateStr, record = record, onEmojiClick = { selectedEmoji ->
                    // 클릭 시 DayDetailDialog 열기
                    selectedDate = dateStr
                    // 나중에 selectedEmoji 값도 Dialog에 넘길 수 있음
                })

                Spacer(modifier = Modifier.height(1.dp))
            }
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
                    imageUri = imageUri?.toString(),
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

    // Y2K 느낌의 파스텔 배경색
    val backgroundColor = when (dayofWeek) {
        DayOfWeek.MONDAY -> Color(0xFFF0F8FF) // AliceBlue
        DayOfWeek.TUESDAY -> Color(0xFFFAEBD7) // MintCream
        DayOfWeek.WEDNESDAY -> Color(0xFFF0FFFF) // LemonChiffon
        DayOfWeek.THURSDAY -> Color(0xFFE0FFFF) // LightCyan
        DayOfWeek.FRIDAY -> Color(0xFFFFFACD) // Azure
        DayOfWeek.SATURDAY -> Color(0xFFFFF0F5) // AntiqueWhite
        DayOfWeek.SUNDAY -> Color(0xFFF5FFFA) // LavenderBlush
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // 높이 약간 증가
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
                Card(
                    shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
                    colors = CardDefaults.cardColors(containerColor = backgroundColor), // 요일별 배경색
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp), // 그림자 약간 줄임
                    modifier = Modifier
                        .fillMaxSize()
                        /*.border(
                            1.dp,
                            colorResource(id = R.color.y2k_border),
                            RoundedCornerShape(16.dp)
                        ) // Y2K 테두리*/
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 30.dp, vertical = 10.dp) // 패딩 조정
                            .background(Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // 요소 간 간격 조절
                    ) {
                        Column(
                            modifier = Modifier.weight(1f) // 텍스트가 공간을 더 차지하도록
                        ) {
                            // 요일 인디케이터 (미니멀리스트 텍스트)
                            Text(
                                text = localDate.dayOfWeek.toString(),
                                fontFamily = CustomFontFamily,
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.y2k_text).copy(alpha = 0.6f), // 연한 색상
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            // 날짜 텍스트
                            Text(
                                text = localDate.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                        "MM/dd (E)"
                                    )
                                ),
                                fontFamily = CustomFontFamily,
                                fontSize = 28.sp,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.W600
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // 요약 텍스트 (있으면 표시)
                            record?.summary?.let { summary ->
                                Text(
                                    text = summary,
                                    fontFamily = CustomFontFamily,
                                    fontSize = 18.sp,
                                    color = colorResource(id = R.color.y2k_text).copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 이모지 및 클릭 영역
                        record?.emojiResID?.let { emojiResID ->
                            Image(
                                painter = painterResource(id = emojiResID),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp) // 이모지 크기 증가
                                    .clickable { onEmojiClick(record.emojiResID.toString()) } // 클릭 가능하도록
                                    .padding(4.dp) // 패딩 추가
                            )
                        } ?: run { // 기록이 없을 때 이모지 선택 버튼 표시
                            Image(
                                painter = painterResource(id = R.drawable.emoji_add), // '+' 이모지 아이콘 (가정)
                                contentDescription = "Add Mood",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { onEmojiClick("add") } // 클릭 가능하도록
                                    .padding(4.dp)
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
    onSave: (Int?, String, String, String?, androidx.compose.ui.graphics.ImageBitmap?) -> Unit
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
    var imageUri by remember { mutableStateOf<String?>(initialRecord?.imageUri) }
    var cameraBitmap by remember { mutableStateOf(initialRecord?.imageBitmap) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri.value != null) {
            imageUri = photoUri.value.toString()
            cameraBitmap = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
            cameraBitmap = null
        }
    }

    fun launchCamera() {
        if (hasCameraPermission) {
            val file = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            photoUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ){
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp)
                    .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.y2k_background))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💖 ${LocalDate.parse(date).dayOfWeek}의 기록 💖",
                        fontFamily = CustomFontFamily,
                        fontSize = 22.sp,
                        color = colorResource(id = R.color.y2k_text)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. 한 줄 요약
                    TextField(
                        value = summary,
                        onValueChange = { summary = it },
                        placeholder = { Text("한 줄 요약...", fontFamily = CustomFontFamily, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp), clip = false)
                            .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = colorResource(id = R.color.y2k_primary),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text)),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. 상세 기록
                    TextField(
                        value = detail,
                        onValueChange = { detail = it },
                        placeholder = { Text("자세한 이야기를 들려주세요...", fontFamily = CustomFontFamily, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(8.dp))
                            .shadow(4.dp, RoundedCornerShape(8.dp), clip = false),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = colorResource(id = R.color.y2k_primary),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. 이모지 선택
                    Text("✨ 오늘의 기분은? ✨", fontFamily = CustomFontFamily, fontSize = 18.sp, color = colorResource(id = R.color.y2k_text))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        emojiOptions.forEach { resID ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable { selectedEmojiRes = resID }
                                    .background(
                                        if (resID == selectedEmojiRes) colorResource(id = R.color.y2k_primary) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                /*.border(
                                    width = 2.dp,
                                    color = if (resID == selectedEmojiRes) colorResource(id = R.color.y2k_border) else Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                ),*/
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = resID),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 사진 미리보기
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(it)),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.y2k_secondary)),
                            border = BorderStroke(0.5.dp, colorResource(id = R.color.y2k_border)) ,

                            ) {
                            Text(" 갤러리 ", fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text))
                        }
                        Button(
                            onClick = { launchCamera() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.y2k_secondary)),
                            border = BorderStroke(0.5.dp, colorResource(id = R.color.y2k_border))
                        ) {
                            Text("사진찍기", fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text))
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    // 저장 및 취소 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { onSave(selectedEmojiRes, summary, detail, imageUri, cameraBitmap) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.y2k_primary)),
                            border = BorderStroke(0.5.dp, colorResource(id = R.color.y2k_border)),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text("저장할래!", fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, colorResource(id = R.color.y2k_border)),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text("다음에...", fontFamily = CustomFontFamily, color = colorResource(id = R.color.y2k_text))
                        }
                    }
                }
            }
        }
    }
}