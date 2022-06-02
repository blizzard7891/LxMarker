package com.example.lxmarker.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "check_in")
data class CheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val imei: String,
    val x: String,
    val y: String,
    val z: String
) {
    val idText: String
        get() = id.toString()

    val timeFormatText: String
        get() {
            val replacedTime = time.replace(" ", "")
            val middle = replacedTime.length / 2
            return replacedTime.substring(0, middle) + "\n" + time.substring(middle, time.length)
        }

    val imeiFormatText: String
        get() {
            val middle = imei.length / 2
            return imei.substring(0, middle) + "\n" + imei.substring(middle, imei.length)
        }
}