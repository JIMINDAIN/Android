package com.example.mentalnote.ui

import android.widget.Toast
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalnote.R
import com.example.mentalnote.ui.theme.CustomFontFamily
import com.example.mentalnote.data.database.AppDatabase
import com.example.mentalnote.data.model.Friend
import com.example.mentalnote.data.model.FriendRequest
import com.example.mentalnote.data.model.User
import com.example.mentalnote.dataStore
import com.example.mentalnote.USER_USERNAME
import kotlinx.coroutines.flow.first

@Composable
fun AddFriendScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var friendUsername by remember { mutableStateOf("") }

    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { db.userDao() }
    val friendDao = remember { db.friendDao() }
    val friendRequestDao = remember { db.friendRequestDao() }

    val currentUserId = remember { mutableStateOf(-1) }
    val currentUsername = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val usernameFromDataStore = context.dataStore.data.first()[USER_USERNAME] ?: ""
        currentUsername.value = usernameFromDataStore

        val user = userDao.getUserByUsername(usernameFromDataStore)
        currentUserId.value = user?.id ?: -1
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0xFFADD8E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "친구 요청",
                fontFamily = CustomFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.y2k_primary)
            )
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = friendUsername,
                onValueChange = { friendUsername = it },
                label = { Text("친구 사용자 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (friendUsername.isBlank()) {
                            Toast.makeText(context, "친구 사용자 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        if (friendUsername == currentUsername.value) {
                            Toast.makeText(context, "자기 자신에게 친구 요청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        if (currentUserId.value == -1) {
                            Toast.makeText(context, "현재 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 1. 친구로 요청하려는 사용자가 Room DB에 존재하는지 확인
                        val targetUser = userDao.getUserByUsername(friendUsername)

                        if (targetUser == null) {
                            Toast.makeText(context, "존재하지 않는 사용자 이름입니다.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 2. 이미 친구인지 확인
                        val existingFriend = friendDao.getFriend(currentUserId.value, targetUser.id)
                        if (existingFriend != null) {
                            Toast.makeText(context, "이미 친구로 추가된 사용자입니다.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 3. 이미 친구 요청을 보냈는지 확인
                        val existingRequest = friendRequestDao.getFriendRequest(currentUserId.value, targetUser.id)
                        if (existingRequest != null) {
                            Toast.makeText(context, "이미 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 4. 상대방이 나에게 친구 요청을 보냈는지 확인 (상대방이 보낸 요청을 내가 수락할 수 있도록)
                        val incomingRequest = friendRequestDao.getFriendRequest(targetUser.id, currentUserId.value)
                        if (incomingRequest != null) {
                            Toast.makeText(context, "상대방이 이미 친구 요청을 보냈습니다. 친구 요청 목록에서 확인해주세요.", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        // 5. 친구 요청 보내기
                        val friendRequest = FriendRequest(
                            senderUserId = currentUserId.value,
                            receiverUserId = targetUser.id,
                            senderUsername = currentUsername.value,
                            receiverUsername = targetUser.username
                        )
                        friendRequestDao.insertFriendRequest(friendRequest)

                        Toast.makeText(context, "${friendUsername} 님에게 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                        friendUsername = "" // 입력 필드 초기화
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
                    "친구 요청 보내기",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6))
            ) {
                Text(
                    "뒤로 가기",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
