package com.example.lxmarker.util

import kotlin.ByteArray

object ByteArray {

    fun ByteArray.toLittleEndian(): Int {
        var result = 0
        for (i in this.indices) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result
    }

    fun ByteArray.toInt(): Int {
        var result = 0
        var shift = 0
        for (byte in this) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun ByteArray.toHexString(): String {
        val builder = StringBuilder()
        for (b in this) {
            builder.append(String.format("%02x ", b))
        }

        return builder.toString()
    }

    fun ByteArray.hexToAscii(): String {
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