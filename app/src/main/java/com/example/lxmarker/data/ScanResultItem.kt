package com.example.lxmarker.data

import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanResult
import androidx.lifecycle.MutableLiveData

data class ScanResultItem(
    val scanResult: ScanResult,
    var gattService: BluetoothGattService? = null,
    val connected: MutableLiveData<Boolean> = MutableLiveData(),
    val title: String = ""
)
