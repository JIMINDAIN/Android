package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

<<<<<<< HEAD
@Entity(tableName = "friend_requests", primaryKeys = ["senderUserId", "receiverUserId"])
data class FriendRequest(
    val senderUserId: String,
    val receiverUserId: String,
=======
@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUserId: Int,
    val receiverUserId: Int,
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
    val senderUsername: String,
    val receiverUsername: String
)