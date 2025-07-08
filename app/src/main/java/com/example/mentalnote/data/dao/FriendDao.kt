package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mentalnote.data.model.Friend

@Dao
interface FriendDao {
    @Insert
    suspend fun insertFriend(friend: Friend)

    @Query("SELECT * FROM friends WHERE userId = :userId")
    suspend fun getFriendsForUser(userId: Int): List<Friend>

    @Query("DELETE FROM friends WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun deleteFriend(userId: Int, friendUserId: Int)

    @Query("SELECT * FROM friends WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun getFriend(userId: Int, friendUserId: Int): Friend?
}
