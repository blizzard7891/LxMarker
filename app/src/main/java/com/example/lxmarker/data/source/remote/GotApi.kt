package com.example.lxmarker.data.source.remote

import com.example.lxmarker.data.source.remote.request.UploadCheckInRequest
import com.example.lxmarker.data.source.remote.response.ResponseBody
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GotApi {

    @Headers("Content-Type: application/json")
    @POST("api/log/advertising/")
    fun requestUpload(@Body request: UploadCheckInRequest): Single<Response<ResponseBody>>
}