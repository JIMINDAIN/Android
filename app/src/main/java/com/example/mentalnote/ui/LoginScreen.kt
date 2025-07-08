package com.example.mentalnote.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.mentalnote.IS_LOGGED_IN
import com.example.mentalnote.R
import com.example.mentalnote.USER_PASSWORD
import com.example.mentalnote.USER_USERNAME
import com.example.mentalnote.dataStore
import com.example.mentalnote.ui.theme.CustomFontFamily
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0xFFADD8E6)) // Y2K 느낌의 푸른색 배경
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 앱 로고/타이틀
            Text(
                text = "MENTAL NOTE",
                fontFamily = CustomFontFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.y2k_primary), // Y2K Primary Color
                modifier = Modifier.graphicsLayer {
                    translationY = -100f // 위로 살짝 이동
                    shadowElevation = 8.dp.toPx()
                    spotShadowColor = Color.Black.copy(alpha = 0.5f)
                }
            )
            Spacer(modifier = Modifier.height(64.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("사용자 이름") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 로그인 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        val prefs = context.dataStore.data.first()
                        val storedUsername = prefs[USER_USERNAME]
                        val storedPassword = prefs[USER_PASSWORD]

                        if (username == storedUsername && password == storedPassword) {
                            context.dataStore.edit { it[IS_LOGGED_IN] = true }
                            Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "사용자 이름 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500))
            ) {
                Text(
                    "로그인",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 회원가입 버튼
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6))
            ) {
                Text(
                    "회원가입",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
