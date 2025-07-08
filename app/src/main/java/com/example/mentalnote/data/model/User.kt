package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val username: String,
    val email: String,
    val profileImageUrl: String?
)
