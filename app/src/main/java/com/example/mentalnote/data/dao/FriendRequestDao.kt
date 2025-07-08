package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mentalnote.data.model.FriendRequest

@Dao
interface FriendRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE receiverUserId = :receiverUserId")
    suspend fun getFriendRequestsForUser(receiverUserId: String): List<FriendRequest>

    @Query("DELETE FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun deleteFriendRequest(senderUserId: String, receiverUserId: String)

    @Query("SELECT * FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun getFriendRequest(senderUserId: String, receiverUserId: String): FriendRequest?
}