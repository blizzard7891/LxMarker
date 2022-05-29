package com.example.lxmarker.data.repository

import androidx.lifecycle.LiveData
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.source.CheckInDao
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class MarkerRepository @Inject constructor(
    private val checkInDao: CheckInDao
) {
    fun getAllCheckIn(): List<CheckIn> = checkInDao.getAll()

    fun insertCheckIn(checkIn: CheckIn) = checkInDao.insert(checkIn)

    fun deleteAllCheckIn() = checkInDao.deleteAll()
}