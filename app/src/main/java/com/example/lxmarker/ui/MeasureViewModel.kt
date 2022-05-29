package com.example.lxmarker.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.util.ByteArray.toHexString
import com.example.lxmarker.util.ByteArray.toLittleEndian
import javax.inject.Inject

class MeasureViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    val scanItem: MutableLiveData<ScanResultItem> = MutableLiveData()
    private val distance: MutableLiveData<Float> = MutableLiveData(0f)
    val distanceText: LiveData<String> = distance.map {
        String.format("%.2fM", it)
    }

    fun setItem(item: ScanResultItem) {
        scanItem.value = item
    }

    fun setReadByteArray(byteArray: ByteArray) {
        val stxIndex = byteArray.withIndex().find { (_, byte) -> byte == VALUE_STX }?.index ?: return

        var currentIndex = stxIndex
        //  uint8_t stx;
        val stx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        Log.d(TAG, "stx: ${stx.toHexString()}")
        currentIndex++
        //	uint8_t select;
        val select = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        Log.d(TAG, "select: ${select.toHexString()}")
        currentIndex++
        //	uint16_t distance;
        val distance = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        Log.d(TAG, "distance: ${distance.toHexString()}, ${distance.toLittleEndian()}")
        currentIndex += 2
        //	uint8_t  etc[8];
        val etc = byteArray.copyOfRange(currentIndex, currentIndex + 8)
        Log.d(TAG, "etc: ${etc.toHexString()}")
        currentIndex += 8
        //	int16_t x_axis;
        val xAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        Log.d(TAG, "xAxis: ${xAxis.toHexString()}")
        currentIndex += 2
        //	int16_t y_axis;
        val yAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        Log.d(TAG, "yAxis: ${yAxis.toHexString()}")
        currentIndex += 2
        //	int16_t z_axis;
        val zAxis = byteArray.copyOfRange(currentIndex, currentIndex + 2)
        Log.d(TAG, "zAxis: ${zAxis.toHexString()}")
        currentIndex += 2
        //	uint8_t etx;
        val etx = byteArray.copyOfRange(currentIndex, currentIndex.inc())
        Log.d(TAG, "etx: ${etx.toHexString()}")

        if (select.firstOrNull() == 0x00.toByte()) {
            this.distance.postValue(distance.toLittleEndian() / 100f)
        }
    }

    private companion object {
        const val TAG = "MeasureViewModel"
        const val VALUE_STX = 0xA5.toByte()
    }
}