package com.example.mentalnote


//import com.example.mentalnote.ui.FriendScreen
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mentalnote.model.DayRecord
import com.example.mentalnote.ui.AddFriendScreen
import com.example.mentalnote.ui.FriendRequestListScreen
import com.example.mentalnote.ui.GalleryTab
import com.example.mentalnote.ui.LoginScreen
import com.example.mentalnote.ui.MonthTab
import com.example.mentalnote.ui.ProfileScreen
import com.example.mentalnote.ui.RegistrationScreen
import com.example.mentalnote.ui.WeekTab
import com.example.mentalnote.ui.loadDayRecords
import com.example.mentalnote.ui.saveDayRecords
import com.example.mentalnote.ui.theme.MentalNoteTheme
import com.example.mentalnote.util.NotificationScheduler
import com.example.mentalnote.util.loadDummyJsonRecords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

enum class AuthScreen {
    LOGIN,
    REGISTER
}

enum class MainScreenState {
    WEEK,
    GALLERY,
    MONTH,
    PROFILE,
    ADDFRIEND,
    FRIEND_REQUESTS
}


// ... (기존 코드)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationScheduler.createNotificationChannel(applicationContext)

        // Exact Alarm 권한 확인 및 요청
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) 이상
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        setContent {
            MentalNoteTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var currentAuthScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    val prefs = context.dataStore.data.first()
                    isLoggedIn = prefs[IS_LOGGED_IN] ?: false
                }

                if (isLoggedIn) {
                    MainScreen()
                } else {
                    when (currentAuthScreen) {
                        AuthScreen.LOGIN -> LoginScreen(
                            onLoginSuccess = { isLoggedIn = true },
                            onRegisterClick = { currentAuthScreen = AuthScreen.REGISTER }
                        )
                        AuthScreen.REGISTER -> RegistrationScreen(
                            onRegistrationSuccess = { isLoggedIn = true },
                            onBackToLogin = { currentAuthScreen = AuthScreen.LOGIN }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Week", "Gallery", "Month", "Profile") // Add Friend tab
    val tabIcons = listOf(
        R.drawable.week_bottombar,
        R.drawable.gallery_bottombar,
        R.drawable.month_bottombar,
        R.drawable.friend_bottombar
    )
    var currentMainScreenState by remember { mutableStateOf(MainScreenState.WEEK) }

    val context = LocalContext.current
    var dayRecords by remember { mutableStateOf(listOf<DayRecord>()) }
    val backgroundColor = Color(0xFFEAFDF9)
    val nanumFont1 = FontFamily(Font(R.font.dunggeunmo))

    LaunchedEffect(Unit){
        val prefs = context.dataStore.data.first()
        val alreadyInitialized = prefs[stringPreferencesKey("init_dummy")] == "true"
        /*if (!alreadyInitialized) {
            try {
                val dummyRecords = loadDummyJsonRecords(context)
                saveDayRecords(context, dummyRecords)
                context.dataStore.edit {
                    it[stringPreferencesKey("init_dummy")] = "true"
                }
                Log.d("DUMMY", "초기 더미 데이터 저장 완료")
                withContext(Dispatchers.Main) {
                    dayRecords = dummyRecords.sortedBy { it.date }
                }
            } catch (e: Exception) {
                Log.e("DUMMY", "더미 데이터 로딩 실패: ${e.message}")
                withContext(Dispatchers.Main) {
                    dayRecords = emptyList()
                }
            }
        } else {
            val records = loadDayRecords(context)
            withContext(Dispatchers.Main) {
                dayRecords = records.sortedBy { it.date }
            }
        }*/

        /*if (!alreadyInitialized) {
            val dummyRecords = loadDummyJsonRecords(context)
            saveDayRecords(context, dummyRecords)
            dayRecords = dummyRecords
            context.dataStore.edit {
                it[stringPreferencesKey("init_dummy")] = "true"
            }
            Log.d("DUMMY", "초기 더미 데이터 저장 완료")
        } else {
            val loadedRecords = loadDayRecords(context)
            dayRecords = loadedRecords
        }*/



        withContext(Dispatchers.IO) {
            val dummyRecords = loadDummyJsonRecords(context)
            saveDayRecords(context, dummyRecords)
            context.dataStore.edit {
                it[stringPreferencesKey("init_dummy")] = "ignored"
            }
            Log.d("DUMMY", "더미 데이터 강제 저장 완료")
            withContext(Dispatchers.Main) {
                dayRecords = dummyRecords
            }
        }

        /*val records = loadDayRecords(context).toMutableList()

        if (records.isEmpty()) {
            val dummy = loadDummyJsonRecordsOnce(context)
            if (dummy.isNotEmpty()) {
                records.addAll(dummy)
                saveDayRecords(context, records)
            }
        }

        dayRecords = records*/
    }

    LaunchedEffect(Unit) {
        val all = loadDayRecords(context)
        all.forEach {
            Log.d("RECORD_DEBUG", "날짜: ${it.date}, emoji: ${it.emojiResID}")
        }
    }

    // Update currentMainScreenState based on selectedTab
    LaunchedEffect(selectedTab) {
        currentMainScreenState = when (selectedTab) {
            0 -> MainScreenState.WEEK
            1 -> MainScreenState.GALLERY
            2 -> MainScreenState.MONTH
            3 -> MainScreenState.PROFILE
            else -> MainScreenState.WEEK // Default case
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)

    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar (
                    containerColor = Color(0xFFFFFCF2),

                ){
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = {
                                Image(
                                    painter = painterResource(id = tabIcons[index]),
                                    contentDescription = title,
                                    modifier = Modifier.size(56.dp)
                                        .padding(top = 2.dp)
                                )
                                   },
                            /*label = {
                                Text(
                                    text = title,
                                    fontFamily = nanumFont1,
                                    fontSize = 15.sp
                                )},*/
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
                when (currentMainScreenState) {
                    MainScreenState.WEEK -> WeekTab(
                        dayRecords = dayRecords,
                        onSave = { record ->
                            dayRecords = dayRecords.toMutableList().also { list ->
                                val idx = list.indexOfFirst { it.date == record.date }
                                if (idx >= 0) list[idx] = record else list.add(record)
                            }
                        }
                    )
                    MainScreenState.GALLERY -> GalleryTab(dayRecords = dayRecords)
                    MainScreenState.MONTH -> MonthTab(dayRecords = dayRecords)
                    MainScreenState.PROFILE -> ProfileScreen(
                        onAddFriendClick = { currentMainScreenState = MainScreenState.ADDFRIEND },
                        onFriendRequestListClick = { currentMainScreenState = MainScreenState.FRIEND_REQUESTS }
                    )
                    MainScreenState.ADDFRIEND -> AddFriendScreen(onBack = { currentMainScreenState = MainScreenState.PROFILE })
                    MainScreenState.FRIEND_REQUESTS -> FriendRequestListScreen(onBack = { currentMainScreenState = MainScreenState.PROFILE })
                }
            }
        }
    }
}
