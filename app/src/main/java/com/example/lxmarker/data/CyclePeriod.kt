package com.example.lxmarker.data

enum class CyclePeriod(val cmdName: String) {
    MONTH("S3E"), DAY("S2E"), HOUR("S1E");

    companion object {
        fun convertFromString(input: String): CyclePeriod {
            return when (input) {
                HOUR.cmdName -> HOUR
                DAY.cmdName -> DAY
                else -> MONTH
            }
        }
    }
}