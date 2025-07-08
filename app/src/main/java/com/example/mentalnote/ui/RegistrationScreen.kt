package com.example.mentalnote.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.mentalnote.IS_LOGGED_IN
import com.example.mentalnote.USER_PASSWORD
import com.example.mentalnote.USER_USERNAME
import com.example.mentalnote.dataStore
import com.example.mentalnote.ui.theme.CustomFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0xFFADD8E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
                //.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "회원가입",
                fontFamily = CustomFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(64.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일 주소") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    coroutineScope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[USER_USERNAME] = email
                                            prefs[USER_PASSWORD] = password // 실제 앱에서는 비밀번호를 해싱하여 저장해야 합니다.
                                            prefs[IS_LOGGED_IN] = true
                                        }
                                    }
                                    Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                    onRegistrationSuccess()
                                } else {
                                    Toast.makeText(context, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp),
                //.border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 2.dp,
                    focusedElevation = 3.dp
                ),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF0F5))
            ) {
                Text(
                    "회원가입 완료",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp),
                //.border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 2.dp,
                    focusedElevation = 3.dp
                ),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFDF0))
            ) {
                Text(
                    "로그인 화면으로 돌아가기",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}
