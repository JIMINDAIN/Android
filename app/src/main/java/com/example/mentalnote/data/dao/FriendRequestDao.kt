package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mentalnote.data.model.FriendRequest

@Dao
interface FriendRequestDao {
    @Insert
    suspend fun insertFriendRequest(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE receiverUserId = :receiverUserId")
    suspend fun getFriendRequestsForUser(receiverUserId: Int): List<FriendRequest>

    @Query("DELETE FROM friend_requests WHERE id = :requestId")
    suspend fun deleteFriendRequest(requestId: Int)

    @Query("SELECT * FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun getFriendRequest(senderUserId: Int, receiverUserId: Int): FriendRequest?
}