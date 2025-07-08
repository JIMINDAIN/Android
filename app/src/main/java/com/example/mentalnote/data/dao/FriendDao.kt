package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mentalnote.data.model.Friend

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Query("SELECT * FROM friends WHERE userId = :userId")
    suspend fun getFriendsForUser(userId: String): List<Friend>

    @Query("DELETE FROM friends WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun deleteFriend(userId: String, friendUserId: String)

    @Query("SELECT * FROM friends WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun getFriend(userId: String, friendUserId: String): Friend?
}
