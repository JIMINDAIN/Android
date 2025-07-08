package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

<<<<<<< HEAD
@Entity(tableName = "friends", primaryKeys = ["userId", "friendUserId"])
data class Friend(
    val userId: String,
    val friendUserId: String,
=======
@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val friendUserId: Int,
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
    val friendUsername: String
)
