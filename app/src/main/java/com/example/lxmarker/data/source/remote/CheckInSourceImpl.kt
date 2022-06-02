package com.example.lxmarker.data.source.remote

import android.util.Log
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.source.remote.request.UploadCheckInRequest
import com.example.lxmarker.data.source.remote.response.ResponseBody
import com.example.lxmarker.util.Global
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class CheckInSourceImpl @Inject constructor() {

    private val gotAPi = Retrofit.Builder()
        .baseUrl(Global.BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GotApi::class.java)

    fun requestUpload(checkIn: CheckIn): Single<ResponseBody> {
        return gotAPi.requestUpload(mapToCheckInRequest(checkIn))
            .flatMap { response ->
                if (response.isSuccessful) {
                    Single.fromCallable { response.body() ?: ResponseBody(null, null, null) }
                } else {
                    Single.error(Throwable(response.message()))
                }
            }
            .doOnSuccess { Log.d(TAG, "requestUpload success: $it") }
            .doOnError { Log.e(TAG, "requestUpload error: $it") }
    }

    private fun mapToCheckInRequest(checkIn: CheckIn): UploadCheckInRequest {
        return UploadCheckInRequest(
            eventDt = checkIn.time,
            phone = checkIn.imei,
            mac = checkIn.imei,
            x = checkIn.x,
            y = checkIn.y,
            z = checkIn.z,
        )
    }

    private companion object {
        const val TAG = "CheckInSourceImpl"
    }
}