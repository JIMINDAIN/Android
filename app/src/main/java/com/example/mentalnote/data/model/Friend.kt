package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val friendUserId: Int,
    val friendUsername: String
)