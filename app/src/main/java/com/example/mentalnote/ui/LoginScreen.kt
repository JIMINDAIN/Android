package com.example.mentalnote.ui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mentalnote.dataStore
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mentalnote.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mentalnote.ui.theme.CustomFontFamily
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.ui.res.colorResource

val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    // Kakao Login
    val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "카카오 로그인 실패", error)
        } else if (token != null) {
            Log.d(TAG, "카카오 로그인 성공 ${token.accessToken}")
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[IS_LOGGED_IN] = true
                }
            }
            onLoginSuccess()
        }
    }

    val kakaoLoginWithKakaoTalk: () -> Unit = {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
                } else if (token != null) {
                    Log.d(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    CoroutineScope(Dispatchers.IO).launch {
                        context.dataStore.edit { prefs ->
                            prefs[IS_LOGGED_IN] = true
                        }
                    }
                    onLoginSuccess()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
        }
    }

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

            // 카카오 로그인 버튼
            Button(
                onClick = kakaoLoginWithKakaoTalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp) // 버튼 높이 증가
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)), // 각진 테두리
                shape = CutCornerShape(12.dp), // 각진 모서리
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500)) // Kakao yellow
            ) {
                Text(
                    "카카오 로그인",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
