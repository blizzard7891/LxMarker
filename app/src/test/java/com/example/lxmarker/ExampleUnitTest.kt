package com.example.lxmarker

import android.util.Log
import com.example.lxmarker.ui.ActivityViewModel
import com.example.lxmarker.util.ByteArray.toLittleEndian
import com.example.lxmarker.util.HexDump
import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val xAxis = HexDump.hexStringToByteArray("b1ff").toLittleEndian().toDouble().also {
            println("xAxis: ${-it}")
        }
        val yAxis = HexDump.hexStringToByteArray("e2ff").toLittleEndian().toDouble().also {
            println("yAxis: $it")
        }
        val zAxis = HexDump.hexStringToByteArray("e207").toLittleEndian().toDouble().also {
            println("zAxis: $it")
        }

        var accX = atan(xAxis / sqrt(yAxis.pow(2) + zAxis.pow(2)))
        accX *= ActivityViewModel.RADIAN_TO_DEGREE
        val accY = atan((-xAxis) / sqrt(yAxis.pow(2) + zAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
        val accZ = atan(zAxis / sqrt((-xAxis).pow(2) + yAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
//        val accX = atan(yAxis / sqrt(xAxis.pow(2) + zAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
//        val accY = atan((-xAxis) / sqrt(yAxis.pow(2) + zAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
//        val accZ = atan(zAxis / sqrt((-xAxis).pow(2) + yAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE

        println("accX: $accX, accY: $accY, accZ: $accZ")
    }

    @Test
    fun timeFormat() {
        val simpleDateFormat = SimpleDateFormat("yy.MM.dd\nHH:mm:ss")
        val stamp = System.currentTimeMillis()
        val date = Date(stamp)
        println(simpleDateFormat.format(date))
    }
}