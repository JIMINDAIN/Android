package com.example.mentalnote.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalnote.R
import com.example.mentalnote.USER_USERNAME
import com.example.mentalnote.data.database.AppDatabase
import com.example.mentalnote.data.model.Friend
import com.example.mentalnote.data.model.FriendRequest
import com.example.mentalnote.dataStore
import com.example.mentalnote.ui.theme.CustomFontFamily
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun FriendRequestListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { db.userDao() }
    val friendDao = remember { db.friendDao() }
    val friendRequestDao = remember { db.friendRequestDao() }

    val currentUserId = remember { mutableStateOf(-1) }
    val friendRequests = remember { mutableStateListOf<FriendRequest>() }

    LaunchedEffect(Unit) {
        val usernameFromDataStore = context.dataStore.data.first()[USER_USERNAME] ?: ""
        val user = userDao.getUserByUsername(usernameFromDataStore)
        currentUserId.value = user?.id ?: -1

        if (currentUserId.value != -1) {
            friendRequests.addAll(friendRequestDao.getFriendRequestsForUser(currentUserId.value))
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        //.background(color = Color(0xFFADD8E6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "친구 요청 목록",
                fontFamily = CustomFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
                //color = colorResource(id = R.color.y2k_primary)
            )
            Spacer(modifier = Modifier.height(45.dp))

            if (friendRequests.isEmpty()) {
                Text(
                    text = " 받은 친구 요청이 없습니다.",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(friendRequests) {
                        FriendRequestItem(
                            friendRequest = it,
                            onAccept = {
                                coroutineScope.launch {
                                    // 1. 친구 관계 추가 (양방향)
                                    val newFriendForCurrentUser = Friend(
                                        userId = it.receiverUserId,
                                        friendUserId = it.senderUserId,
                                        friendUsername = it.senderUsername
                                    )
                                    friendDao.insertFriend(newFriendForCurrentUser)

                                    val newFriendForSender = Friend(
                                        userId = it.senderUserId,
                                        friendUserId = it.receiverUserId,
                                        friendUsername = it.receiverUsername
                                    )
                                    friendDao.insertFriend(newFriendForSender)

                                    // 2. 친구 요청 삭제
                                    friendRequestDao.deleteFriendRequest(it.id)
                                    friendRequests.remove(it)
                                    Toast.makeText(context, "${it.senderUsername} 님과 친구가 되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onReject = {
                                coroutineScope.launch {
                                    // 친구 요청 삭제
                                    friendRequestDao.deleteFriendRequest(it.id)
                                    friendRequests.remove(it)
                                    Toast.makeText(context, "${it.senderUsername} 님의 친구 요청을 거절했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp,
                    focusedElevation = 4.dp
                ),
                    //.border(2.dp, colorResource(id = R.color.y2k_border), CutCornerShape(12.dp)),
                    //.shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFE7F2))//
            ) {
                Text(
                    "뒤로 가기",
                    fontFamily = CustomFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
fun FriendRequestItem(friendRequest: FriendRequest, onAccept: () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, CutCornerShape(8.dp))
            .border(1.dp, colorResource(id = R.color.y2k_border), CutCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${friendRequest.senderUsername} 님의 친구 요청",
            fontFamily = CustomFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row {
            Button(
                onClick = onAccept,
                modifier = Modifier.width(80.dp),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500))
            ) {
                Text("수락", color = Color.Black, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReject,
                modifier = Modifier.width(80.dp),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6))
            ) {
                Text("거절", color = Color.Black, fontSize = 14.sp)
            }
        }
    }
}