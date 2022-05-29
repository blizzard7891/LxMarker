package com.example.lxmarker

import org.junit.Test

class ParserTest {
    private val testString = "A5 00 61 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5A A5 1B AB 1E FF 06 00 01 0F 20 02 F6 7E 0D 05 D7 5A"
    companion object {
        private const val VALUE_STX = 0xA5.toByte()
    }

    @Test
    fun test() {
        val testData = testString.filter { !it.isWhitespace() }
        val byteArray = testData.decodeHex()
        println("\n-----byteArray: ${byteArray.size}")
        println("${byteArray.toHexString()}\n")

        val stxIndex = byteArray.withIndex().find { (_, byte) -> byte == VALUE_STX }?.index ?: return
        println("stxIndex: $stxIndex")

        var currentIndex = stxIndex
        //  uint8_t stx;
        val stx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        println("stx: ${stx.toHexString()}")
        currentIndex++
        //	uint8_t select;
        val select = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        println("select: ${select.toHexString()}")
        currentIndex++
        //	uint16_t distance;
        val distance = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        println("distance: ${distance.toHexString()}, ${distance.toLittleEndian()}")
        currentIndex += 2
        //	uint8_t  etc[8];
        val etc = byteArray.copyOfRange(currentIndex, currentIndex + 8)
        println("etc: ${etc.toHexString()}")
        currentIndex += 8
        //	int16_t x_axis;
        val xAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        println("xAxis: ${xAxis.toHexString()}")
        currentIndex += 2
        //	int16_t y_axis;
        val yAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        println("xAxis: ${yAxis.toHexString()}")
        currentIndex += 2
        //	int16_t z_axis;
        val zAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        println("xAxis: ${zAxis.toHexString()}")
        currentIndex += 2
        //	uint8_t etx;
        val etx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        println("etx: ${etx.toHexString()}")
    }

    private fun ByteArray.toLittleEndian(): Int {
        var result = 0
        for (i in this.indices) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result
    }

    private fun ByteArray.toInt(): Int {
        var result = 0
        var shift = 0
        for (byte in this) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun ByteArray.toHexString(): String {
        val builder = StringBuilder()
        for (b in this) {
            builder.append(String.format("%02x ", b))
        }

        return builder.toString()
    }

    private fun ByteArray.hexToAscii(): String {
        val s = this.toHexString()
        val n = s.length
        val sb = java.lang.StringBuilder(n / 2)
        var i = 0
        while (i < n) {
            val a = s[i]
            val b = s[i + 1]
            sb.append((hexToInt(a) shl 4 or hexToInt(b)).toChar())
            i += 2
        }
        return sb.toString()
    }

    private fun hexToInt(ch: Char): Int {
        if (ch in 'a'..'f') {
            return ch - 'a' + 10
        }
        if (ch in 'A'..'F') {
            return ch - 'A' + 10
        }
        if (ch in '0'..'9') {
            return ch - '0'
        }
        throw IllegalArgumentException(ch.toString())
    }
}