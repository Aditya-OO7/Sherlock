package com.adityaoo7.sherlock.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adityaoo7.sherlock.data.LoginAccount

@Database(entities = [LoginAccount::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}