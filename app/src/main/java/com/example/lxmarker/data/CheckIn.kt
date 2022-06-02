package com.example.lxmarker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in")
data class CheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val markerNum: String,
    val x: String,
    val y: String,
    val z: String
) {
    val idText: String
        get() = id.toString()

    val timeFormatText: String
        get() {
            val middle = time.length / 2
            return time.substring(0, middle) + "\n" + time.substring(middle, time.length)
        }

    val markerNumFormatText: String
        get() {
            val middle = markerNum.length / 2
            return markerNum.substring(0, middle) + "\n" + markerNum.substring(middle, markerNum.length)
        }
}