package com.example.mentalnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalnote.R
import com.example.mentalnote.USER_FRIENDS
import com.example.mentalnote.USER_USERNAME
import com.example.mentalnote.dataStore
import com.example.mentalnote.ui.theme.CustomFontFamily
import kotlinx.coroutines.flow.first

@Composable
fun ProfileScreen(onAddFriendClick: () -> Unit, onFriendRequestListClick: () -> Unit) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        username = prefs[USER_USERNAME] ?: ""
        friends = prefs[USER_FRIENDS] ?: emptySet()
    }

    Column(){
        AppHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(color = Color(0xFFADD8E6))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "내 프로필",
                fontFamily = CustomFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)), // Light Cyan
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Placeholder for profile image/icon
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorResource(id = R.color.y2k_primary).copy(alpha = 0.7f)),
                            //.border(2.dp, colorResource(id = R.color.y2k_border), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontFamily = CustomFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = username,
                        fontFamily = CustomFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorResource(id = R.color.y2k_text)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Friend Management Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)), // Light Cyan
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "내 친구 목록",
                        fontFamily = CustomFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.y2k_text),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (friends.isEmpty()) {
                        Text(
                            text = "아직 친구가 없습니다.",
                            fontFamily = CustomFontFamily,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.y2k_text),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            friends.forEach { friend ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB0E0E6)), // Powder Blue
                                    //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Text(
                                        text = friend,
                                        fontFamily = CustomFontFamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = onAddFriendClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                            //.border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 2.dp,
                            focusedElevation = 3.dp
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF0F5)) // 배경색과 유사하게
                    ) {
                        Text(
                            "친구 추가",
                            fontFamily = CustomFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3333333)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onFriendRequestListClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                            //.border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                        //shape = CutCornerShape(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 2.dp,
                            focusedElevation = 3.dp
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFE7F2)) // 배경색과 유사하게
                    ) {
                        Text(
                            "친구 요청 목록",
                            fontFamily = CustomFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}