package com.example.lxmarker.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.lxmarker.util.Constants

data class ScanResultItem(
    val scanResult: ScanResult,
    var gatt: BluetoothGatt? = null,
    val connected: MutableLiveData<Boolean> = MutableLiveData(),
    val title: String = ""
) {
    fun getGattService(): BluetoothGattService? {
        return gatt?.services?.find { it.uuid == Constants.Characteristic_UUID }?.also {
            Log.d(TAG, "gattService uuid: ${it.uuid}")
        }
    }

    private companion object {
        const val TAG = "ScanResultItem"
    }
}
