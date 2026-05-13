package com.example.myapplication.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.core.database.dao.UserDao
import com.example.myapplication.core.database.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}