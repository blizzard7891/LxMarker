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
    val z: String,
    val battery: Int
) {
    val idText: String
        get() = id.toString()
}