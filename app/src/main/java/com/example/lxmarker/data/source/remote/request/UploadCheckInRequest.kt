package com.example.lxmarker.data.source.remote.request

data class UploadCheckInRequest(
    val eventDt: String,
    val phone: String,
    val mac: String,
    val x: String,
    val y: String,
    val z: String
)