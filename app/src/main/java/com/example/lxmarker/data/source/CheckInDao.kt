package com.example.lxmarker.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lxmarker.data.CheckIn
import io.reactivex.rxjava3.core.Single

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(checkIn: CheckIn)

    @Query("SELECT * FROM check_in")
    fun getAll(): List<CheckIn>

    @Query("DELETE FROM check_in")
    fun deleteAll()
}