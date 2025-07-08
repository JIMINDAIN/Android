package com.example.mentalnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalnote.R
import com.example.mentalnote.ui.theme.CustomFontFamily
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch
import com.example.mentalnote.dataStore
import com.example.mentalnote.USER_USERNAME
import com.example.mentalnote.USER_PASSWORD
import com.example.mentalnote.IS_LOGGED_IN

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0xFFADD8E6)) // Y2K 느낌의 푸른색 배경
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "회원가입",
                fontFamily = CustomFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.y2k_primary)
            )
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("사용자 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[USER_USERNAME] = username
                                prefs[USER_PASSWORD] = password // 실제 앱에서는 비밀번호를 해싱하여 저장해야 합니다.
                                prefs[IS_LOGGED_IN] = true // 회원가입 성공 시 바로 로그인 상태로 전환
                            }
                            Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            onRegistrationSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500))
            ) {
                Text(
                    "회원가입",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6))
            ) {
                Text(
                    "로그인 화면으로 돌아가기",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}