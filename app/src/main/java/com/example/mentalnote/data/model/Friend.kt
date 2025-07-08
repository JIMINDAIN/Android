package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends", primaryKeys = ["userId", "friendUserId"])
data class Friend(
    val userId: String,
    val friendUserId: String,
    val friendUsername: String
)
