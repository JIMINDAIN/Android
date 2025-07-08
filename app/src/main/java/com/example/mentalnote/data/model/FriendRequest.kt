package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_requests", primaryKeys = ["senderUserId", "receiverUserId"])
data class FriendRequest(
    val senderUserId: String,
    val receiverUserId: String,
    val senderUsername: String,
    val receiverUsername: String
)