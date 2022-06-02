package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.data.repository.MarkerRepository
import com.example.lxmarker.util.ByteArray.toLittleEndian
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: MarkerRepository
) : ViewModel() {

    val checkInList: MutableLiveData<List<CheckInItem>> = MutableLiveData()
    val viewEvent: MutableLiveData<ViewEvent> = LiveEvent()

    private var checkInMap: Map<String, CheckIn> = mutableMapOf()
    var scannable: Boolean = false

    private val currentProcess: HashSet<String> = hashSetOf()

    fun init() {
        Single.fromCallable { repository.getAllCheckIn() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { scannable = true }
            .subscribe({ result ->
                checkInList.value = result.map { CheckInItem.Item(it) }.toMutableList<CheckInItem>().apply {
                    add(0, CheckInItem.Top)
                }

                checkInMap = result.sortedBy { it.time }.associateBy { it.markerNum }
            }, {
                Log.e(TAG, "$it")
            })
    }

    fun clearData() {
        Completable.fromAction {
            repository.deleteAllCheckIn()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                init()
                Log.d(TAG, "clearData complete")
            }, {
                Log.e(TAG, "clearData error: $it")
            })
    }

    fun setScanResult(result: ScanResult) {
        val markerNum = result.device.address
        Log.d(TAG, "setScanResult: $markerNum")

        if (currentProcess.contains(markerNum)) return
        currentProcess.add(markerNum)

        val checkIn = checkInMap[result.device.address]
        if (checkIn == null || canInsertCheckIn(checkIn)) {
            insertCheckIn(parseBleRawData(result))
        } else {
            currentProcess.remove(markerNum)
        }
    }

    private fun canInsertCheckIn(checkIn: CheckIn): Boolean {
        val date = simpleDateFormat.parse(checkIn.time) as Date
        val interval = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)
        return (System.currentTimeMillis() > (date.time + interval))
    }

    private fun insertCheckIn(entity: CheckIn?) {
        if (entity == null) return

        Completable.fromAction {
            repository.insertCheckIn(entity)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    init()
                    currentProcess.remove(entity.markerNum)
                    viewEvent.value = ViewEvent.CheckInFound(entity)
                    Log.d(TAG, "insertCheckIn complete: $entity")
                },
                { Log.e(TAG, "insertCheckIn error: $it") }
            )
    }

    private fun parseBleRawData(result: ScanResult): CheckIn? {
        result.scanRecord
        val byteArray = result.scanRecord!!.bytes
        val stxIndex = byteArray.withIndex().find { (_, byte) -> byte == ActivityViewModel.BLE_VALUE_STX }?.index ?: return null

        var currentIndex = stxIndex
        //  uint8_t stx;
        val stx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
//        Log.d(TAG, "stx: ${stx.toHexString()}")
        currentIndex++
        //	uint8_t select;
        val select = byteArray.copyOfRange(currentIndex, currentIndex.inc())
//        Log.d(TAG, "select: ${select.toHexString()}")
        currentIndex++
        //	uint16_t etc;
        val etc = byteArray.copyOfRange(currentIndex, currentIndex + 2)
//        Log.d(TAG, "etc: ${etc.toHexString()}")
        currentIndex += 2
        //	uint8_t  imei[8];
        val imei = byteArray.copyOfRange(currentIndex, currentIndex + 8)
//        Log.d(TAG, "imei: ${imei.toHexString()}")
        currentIndex += 8
        //	int16_t x_axis;
        val xAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2).toLittleEndian().toDouble()
//        Log.d(TAG, "xAxis: ${xAxis}")

        currentIndex += 2
        //	int16_t y_axis;
        val yAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2).toLittleEndian().toDouble()
//        Log.d(TAG, "yAxis: ${yAxis}")

        currentIndex += 2
        //	int16_t z_axis;
        val zAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2).toLittleEndian().toDouble()
//        Log.d(TAG, "zAxis: ${zAxis}")

        currentIndex += 2
        //	uint8_t etx;
        val etx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
//        Log.d(TAG, "etx: ${etx.toHexString()}")

        val accX = atan(yAxis / sqrt(xAxis.pow(2) + zAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
        val accY = atan((-xAxis) / sqrt(yAxis.pow(2) + zAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE
        val accZ = atan(zAxis / sqrt((-xAxis).pow(2) + yAxis.pow(2))) * ActivityViewModel.RADIAN_TO_DEGREE

//        Log.d(TAG, "accX: $accX, accY: $accY, accZ: $accZ")

        return CheckIn(
            time = getDateString(),
            markerNum = result.device.address,
            x = String.format("%.2f", accX),
            y = String.format("%.2f", accY),
            z = String.format("%.2f", accZ)
        )
    }

    @SuppressLint("SimpleDateFormat")
    private val simpleDateFormat = SimpleDateFormat("yy.MM.dd HH:mm:ss")

    private fun getDateString(): String {
        val stamp = System.currentTimeMillis()
        val date = Date(stamp)
        return simpleDateFormat.format(date)
    }

    private companion object {
        const val TAG = "CheckInViewModel"
    }
}