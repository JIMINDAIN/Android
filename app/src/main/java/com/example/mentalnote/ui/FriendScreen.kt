package com.example.mentalnote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.mentalnote.ui.theme.CustomFontFamily
import androidx.compose.ui.res.colorResource
import com.example.mentalnote.R

@Composable
fun FriendScreen() {
    val context = LocalContext.current
    var friendIdInput by remember { mutableStateOf("") }
    val myMindNoteId = remember { "MindNoteUser1234" } // Placeholder for user's own ID

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "나의 MindNote ID",
            fontFamily = CustomFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.y2k_text)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = myMindNoteId,
            fontFamily = CustomFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorResource(id = R.color.y2k_primary)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "친구 추가",
            fontFamily = CustomFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.y2k_text)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = friendIdInput,
            onValueChange = { friendIdInput = it },
            label = { Text("친구의 MindNote ID 입력", fontFamily = CustomFontFamily) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (friendIdInput.isNotBlank()) {
                    Toast.makeText(context, "'${friendIdInput}'님에게 친구 요청을 보냈습니다! (실제 기능 아님)", Toast.LENGTH_LONG).show()
                    friendIdInput = ""
                } else {
                    Toast.makeText(context, "친구 ID를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("친구 요청 보내기", fontFamily = CustomFontFamily)
        }
    }
}
