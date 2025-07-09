package com.example.mentalnote.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.mentalnote.R
import com.example.mentalnote.USER_BED_TIME
import com.example.mentalnote.USER_WORK_END_TIME
import com.example.mentalnote.dataStore
import com.example.mentalnote.util.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var workEndTime by remember { mutableStateOf("미설정") }
    var bedTime by remember { mutableStateOf("미설정") }
    var showWorkEndTimePicker by remember { mutableStateOf(false) }
    var showBedTimePicker by remember { mutableStateOf(false) }

    val workTimePickerState = rememberTimePickerState()
    val bedTimePickerState = rememberTimePickerState()

    val dunggeunmoFontFamily = FontFamily(Font(R.font.dunggeunmo))

    LaunchedEffect(firebaseAuth.currentUser) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            email = user.email ?: "이메일 정보 없음"
            // Load user profile from Firestore
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nickname = document.getString("nickname") ?: user.displayName ?: ""
                    } else {
                        nickname = user.displayName ?: ""
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error loading user profile", e)
                    nickname = user.displayName ?: ""
                }
        }
    }

    LaunchedEffect(Unit) {
        // Load notification settings from DataStore
        val prefs = context.dataStore.data.first()
        workEndTime = prefs[USER_WORK_END_TIME] ?: "미설정"
        bedTime = prefs[USER_BED_TIME] ?: "미설정"
    }

    fun saveUserProfile() {
        val user = firebaseAuth.currentUser ?: return
        scope.launch {
            try {
                // Save nickname to Firestore
                val userProfile = hashMapOf(
                    "nickname" to nickname
                )

                firestore.collection("users").document(user.uid)
                    .set(userProfile, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                Toast.makeText(context, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error saving profile", e)
                Toast.makeText(context, "프로필 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column {
        AppHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "내 프로필",
                        fontFamily = dunggeunmoFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.y2k_text),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Email Display
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("이메일", fontFamily = dunggeunmoFontFamily) },
                        readOnly = true,
                        textStyle = TextStyle(
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Nickname TextField
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("닉네임", fontFamily = dunggeunmoFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Save Button
                    Button(
                        onClick = { saveUserProfile() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("프로필 저장", fontFamily = dunggeunmoFontFamily)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Notification Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "알림 설정",
                        fontFamily = dunggeunmoFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )

                    // Work End Time Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWorkEndTimePicker = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "퇴근 시간 알림",
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.y2k_text)
                        )
                        Text(
                            text = workEndTime,
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.y2k_primary)
                        )
                    }

                    if (showWorkEndTimePicker) {
                        AlertDialog(
                            onDismissRequest = { showWorkEndTimePicker = false },
                            title = { Text("퇴근 시간 설정", fontFamily = dunggeunmoFontFamily) },
                            text = { TimePicker(state = workTimePickerState) },
                            confirmButton = {
                                Button(onClick = {
                                    showWorkEndTimePicker = false
                                    val selectedTime = String.format("%02d:%02d", workTimePickerState.hour, workTimePickerState.minute)
                                    workEndTime = selectedTime
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[USER_WORK_END_TIME] = selectedTime
                                        }
                                        NotificationScheduler.scheduleNotifications(context)
                                    }
                                }) {
                                    Text("확인", fontFamily = dunggeunmoFontFamily)
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showWorkEndTimePicker = false }) {
                                    Text("취소", fontFamily = dunggeunmoFontFamily)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bed Time Setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBedTimePicker = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "취침 시간 알림",
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.y2k_text)
                        )
                        Text(
                            text = bedTime,
                            fontFamily = dunggeunmoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.y2k_primary)
                        )
                    }

                    if (showBedTimePicker) {
                        AlertDialog(
                            onDismissRequest = { showBedTimePicker = false },
                            title = { Text("취침 시간 설정", fontFamily = dunggeunmoFontFamily) },
                            text = { TimePicker(state = bedTimePickerState) },
                            confirmButton = {
                                Button(onClick = {
                                    showBedTimePicker = false
                                    val selectedTime = String.format("%02d:%02d", bedTimePickerState.hour, bedTimePickerState.minute)
                                    bedTime = selectedTime
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[USER_BED_TIME] = selectedTime
                                        }
                                        NotificationScheduler.scheduleNotifications(context)
                                    }
                                }) {
                                    Text("확인", fontFamily = dunggeunmoFontFamily)
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showBedTimePicker = false }) {
                                    Text("취소", fontFamily = dunggeunmoFontFamily)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
