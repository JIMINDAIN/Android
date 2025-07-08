package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
<<<<<<< HEAD
import androidx.room.OnConflictStrategy
=======
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
import androidx.room.Query
import com.example.mentalnote.data.model.User

@Dao
interface UserDao {
<<<<<<< HEAD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): User?
=======
    @Insert
    suspend fun insertUser(user: User): Long
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
<<<<<<< HEAD
=======

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
}
