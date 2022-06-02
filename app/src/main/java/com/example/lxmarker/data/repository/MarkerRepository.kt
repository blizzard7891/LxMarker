package com.example.lxmarker.data.repository

import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.source.local.CheckInDao
import com.example.lxmarker.data.source.remote.CheckInSourceImpl
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MarkerRepository @Inject constructor(
    private val checkInDao: CheckInDao,
    private val checkInSource: CheckInSourceImpl
) {
    fun getAllCheckIn(): List<CheckIn> = checkInDao.getAll()

    fun insertCheckIn(checkIn: CheckIn, phoneNumber: String): Completable {
        return checkInDao.insert(checkIn)
            .andThen(Single.defer { checkInSource.requestUpload(checkIn, phoneNumber) })
            .ignoreElement()
    }

    fun deleteAllCheckIn() = checkInDao.deleteAll()
}