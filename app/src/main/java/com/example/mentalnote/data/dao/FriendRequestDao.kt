package com.example.mentalnote.data.dao

import androidx.room.Dao
import androidx.room.Insert
<<<<<<< HEAD
import androidx.room.OnConflictStrategy
=======
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
import androidx.room.Query
import com.example.mentalnote.data.model.FriendRequest

@Dao
interface FriendRequestDao {
<<<<<<< HEAD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE receiverUserId = :receiverUserId")
    suspend fun getFriendRequestsForUser(receiverUserId: String): List<FriendRequest>

    @Query("DELETE FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun deleteFriendRequest(senderUserId: String, receiverUserId: String)

    @Query("SELECT * FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun getFriendRequest(senderUserId: String, receiverUserId: String): FriendRequest?
=======
    @Insert
    suspend fun insertFriendRequest(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE receiverUserId = :receiverUserId")
    suspend fun getFriendRequestsForUser(receiverUserId: Int): List<FriendRequest>

    @Query("DELETE FROM friend_requests WHERE id = :requestId")
    suspend fun deleteFriendRequest(requestId: Int)

    @Query("SELECT * FROM friend_requests WHERE senderUserId = :senderUserId AND receiverUserId = :receiverUserId")
    suspend fun getFriendRequest(senderUserId: Int, receiverUserId: Int): FriendRequest?
>>>>>>> 5bc0e5a8120a55b4a6f888c30d5d0e27fccd3ea8
}