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
    suspend fun getFriendsForUser(userId: Int): List<Friend>

    @Query("SELECT * FROM friends WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun getFriendship(userId: Int, friendUserId: Int): Friend?
}
