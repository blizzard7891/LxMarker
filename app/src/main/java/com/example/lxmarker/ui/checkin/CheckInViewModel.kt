package com.example.lxmarker.ui.checkin

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.data.repository.MarkerRepository
import com.example.lxmarker.ui.ActivityViewModel
import com.example.lxmarker.util.ByteArray.toHexString
import com.example.lxmarker.util.ByteArray.toLittleEndian
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private var phoneNumber: String = ""

    fun init(phoneNum: String) {
        phoneNumber = phoneNum
        initData()
    }

    private fun initData() {
        Single.fromCallable { repository.getAllCheckIn() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { scannable = true }
            .subscribe({ result ->
                checkInList.value = result.map { CheckInItem.Item(it) }.toMutableList<CheckInItem>().apply {
                    add(0, CheckInItem.Top)
                }

                checkInMap = result.sortedBy { it.time }.associateBy { it.imei }
            }, {
                Log.e(TAG, "$it")
            }).isDisposed
    }

    fun clearData() {
        Completable.fromAction {
            repository.deleteAllCheckIn()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                initData()
                Log.d(TAG, "clearData complete")
            }, {
                Log.e(TAG, "clearData error: $it")
            }).isDisposed
    }

    fun setScanResult(result: ScanResult) {
        val newCheckIn = parseBleRawData(result) ?: return
        val imei = newCheckIn.imei
        Log.d(TAG, "setScanResult: $imei")

        if (currentProcess.contains(imei)) return
        currentProcess.add(imei)

        val checkIn = checkInMap[imei]
        if (checkIn == null || canInsertCheckIn(checkIn)) {
            insertCheckIn(parseBleRawData(result))
        } else {
            currentProcess.remove(imei)
        }
    }

    private fun canInsertCheckIn(checkIn: CheckIn): Boolean {
        val date = simpleDateFormat.parse(checkIn.time) as Date
        val interval = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)
        return (System.currentTimeMillis() > (date.time + interval))
    }

    private fun insertCheckIn(entity: CheckIn?) {
        if (entity == null) return

        repository.insertCheckIn(entity, phoneNumber)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    initData()
                    currentProcess.remove(entity.imei)
                    viewEvent.value = ViewEvent.CheckInFound(entity)
                    Log.d(TAG, "insertCheckIn complete: $entity")
                },
                { Log.e(TAG, "insertCheckIn error: $it") }
            ).isDisposed
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
            imei = imei.toHexString().replace(" ", ""),
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