package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.data.ViewEvent
import com.hadilq.liveevent.LiveEvent
import javax.inject.Inject

@SuppressLint("MissingPermission")
class SettingViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    val scanResult: MutableLiveData<List<ScanResultItem>> = MutableLiveData(listOf())
    val scanResultEmpty = scanResult.map { it.isEmpty() }

    val viewEvent: LiveEvent<ViewEvent> = LiveEvent()

    fun setScanResults(results: List<ScanResult>) {
        scanResult.value = results.map(::mapToScanResultItem)
    }

    fun setScanResult(result: ScanResult) {
        val viewItem = mapToScanResultItem(result)
        scanResult.value = listOf(viewItem)

        result.device.connectGatt(getApplication(), true, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d(TAG, "onConnectionStateChange status: $status, state: $newState")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "connected to GATT server")
                    viewItem.connected.postValue(true)
                    gatt.discoverServices()
                } else {
                    viewItem.connected.postValue(false)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "service size: ${gatt.services.size}")
                    for (service in gatt.services) {
                        Log.d(TAG, "service uuid: ${service.uuid}")
                        if (service.uuid == Constants.Characteristic_UUID) {
                            viewItem.gattService = service
                        }
                    }
                }
            }
        })
    }

    fun startUwb() {
        viewEvent.value = ViewEvent.Uwb
    }

    fun startCycleSetting() {
        viewEvent.value = ViewEvent.Cycle
    }

    private fun mapToScanResultItem(scanResult: ScanResult): ScanResultItem {
        return ScanResultItem(scanResult = scanResult)
    }

    private companion object {
        const val TAG = "SettingViewModel"
    }
}