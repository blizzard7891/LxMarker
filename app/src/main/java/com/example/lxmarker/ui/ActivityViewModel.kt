package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.lxmarker.R
import com.example.lxmarker.data.CyclePeriod
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.data.ViewEvent
import com.hadilq.liveevent.LiveEvent
import javax.inject.Inject

@SuppressLint("MissingPermission")
class ActivityViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    val scanResult: MutableLiveData<List<ScanResultItem>> = MutableLiveData(listOf())
    val scanResultEmpty = scanResult.map { it.isEmpty() }

    var selectedScanItem: MutableLiveData<ScanResultItem> = MutableLiveData()

    val cyclePeriod: MutableLiveData<CyclePeriod> = MutableLiveData()

    val viewEvent: LiveEvent<ViewEvent> = LiveEvent()

    fun setScanResults(results: List<ScanResult>) {
        scanResult.value = results.map(::mapToScanResultItem)
    }

    fun setScanResult(result: ScanResult) {
        val viewItem = mapToScanResultItem(result)
        scanResult.value = listOf(viewItem)
    }

    fun connectDevice(result: ScanResult? = null) {
        val findToResult = result ?: selectedScanItem.value?.scanResult ?: return

        findViewItem(findToResult)?.let { viewItem ->
            viewItem.scanResult.device.connectGatt(
                getApplication(),
                true,
                object : BluetoothGattCallback() {

                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
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
                            viewItem.gatt = gatt

                            viewItem.getGattService()?.let { service ->
                                viewItem.gatt?.readCharacteristic(service.characteristics[Constants.CYCLE_CMD_CHAR_IDX])
                            }
                        }
                    }

                    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                        super.onCharacteristicChanged(gatt, characteristic)
                        val value = String(characteristic.value)
                        Log.d(TAG, "onCharacteristicChanged = $value, uuid = ${characteristic.uuid}")
                    }

                    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        val value = String(characteristic.value)
                        Log.d(TAG, "onCharacteristicRead = $value, uuid = ${characteristic.uuid}")
                        cyclePeriod.postValue(CyclePeriod.convertFromString(value))
                    }
                })
        }
    }

    fun disConnectGatt() {
        val viewItem = selectedScanItem.value ?: return
        val gatt = viewItem.gatt ?: return
        Log.d(TAG, "disConnectGatt")
        gatt.disconnect()
    }

    fun setCycleSetting(period: CyclePeriod) {
        val viewItem = selectedScanItem.value ?: return
        val gatt = viewItem.gatt ?: return
        val gattService = viewItem.getGattService() ?: return
        Log.d(TAG, "setCycleSetting: $period")

        val characteristic = gattService.characteristics[Constants.CYCLE_CMD_CHAR_IDX].apply {
            setValue(period.cmdName)
            writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        gatt.writeCharacteristic(characteristic)

        cyclePeriod.value = period
        viewEvent.value = ViewEvent.CycleChangeComplete
    }

    fun startUwb(item: ScanResultItem) {
        selectedScanItem.value = item
        viewEvent.value = ViewEvent.Uwb
    }

    fun startCycleSetting(item: ScanResultItem) {
        selectedScanItem.value = item
        viewEvent.value = ViewEvent.Cycle
    }

    private fun findViewItem(result: ScanResult): ScanResultItem? {
        return scanResult.value?.find { it.scanResult == result }
    }

    private fun mapToScanResultItem(scanResult: ScanResult): ScanResultItem {
        return ScanResultItem(scanResult = scanResult)
    }

    private companion object {
        const val TAG = "ActivityViewModel"
    }
}