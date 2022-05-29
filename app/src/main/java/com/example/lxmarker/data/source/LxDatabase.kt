package com.example.lxmarker.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lxmarker.data.CheckIn

@Database(entities = [CheckIn::class], version = 1, exportSchema = false)
abstract class LxDatabase: RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
}