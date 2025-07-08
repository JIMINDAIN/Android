package com.example.mentalnote.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.mentalnote.data.dao.FriendDao
import com.example.mentalnote.data.dao.FriendRequestDao
import com.example.mentalnote.data.dao.UserDao
import com.example.mentalnote.data.model.Friend
import com.example.mentalnote.data.model.FriendRequest
import com.example.mentalnote.data.model.User

@Database(entities = [User::class, Friend::class, FriendRequest::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao
    abstract fun friendRequestDao(): FriendRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mental_note_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
