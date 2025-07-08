package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUserId: Int,
    val receiverUserId: Int,
    val senderUsername: String,
    val receiverUsername: String
)