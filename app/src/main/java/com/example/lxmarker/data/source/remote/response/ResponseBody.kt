package com.example.lxmarker.data.source.remote.response

data class ResponseBody(
    var errCode: Int?,
    var errMsg: String?,
    var cordinate: List<Int>?
)
