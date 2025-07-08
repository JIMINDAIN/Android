package com.example.mentalnote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
<<<<<<< HEAD
    @PrimaryKey val uid: String,
    val username: String,
    val email: String,
    val profileImageUrl: String?
=======
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
)
