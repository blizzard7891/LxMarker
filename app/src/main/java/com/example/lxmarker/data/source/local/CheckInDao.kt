package com.example.lxmarker.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lxmarker.data.CheckIn
import io.reactivex.Completable

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(checkIn: CheckIn): Completable

    @Query("SELECT * FROM check_in")
    fun getAll(): List<CheckIn>

    @Query("DELETE FROM check_in")
    fun deleteAll()
}