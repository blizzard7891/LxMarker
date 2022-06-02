package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.lxmarker.data.CyclePeriod
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.data.repository.MarkerRepository
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class ActivityViewModel @Inject constructor(
    app: Application,
    private val repository: MarkerRepository
) : AndroidViewModel(app) {

    val scanResult: MutableLiveData<List<ScanResultItem>> = MutableLiveData(listOf())
    val scanResultEmpty = scanResult.map { it.isEmpty() }

    var selectedScanItem: MutableLiveData<ScanResultItem> = MutableLiveData()

    val cyclePeriod: MutableLiveData<CyclePeriod> = MutableLiveData()

    val gattServiceDiscovered: MutableLiveData<Boolean> = MutableLiveData()

    val viewEvent: LiveEvent<ViewEvent> = LiveEvent(LiveEventConfig.PreferFirstObserver)

    fun setScanResults(results: List<ScanResult>) {
        scanResult.value = results.map(::mapToScanResultItem)
    }

    fun setScanResult(result: ScanResult) {
        val viewItem = mapToScanResultItem(result)
        if (scanResult.value?.map(ScanResultItem::scanResult)?.contains(result) == true) return

        scanResult.value = listOf(viewItem)
    }

    fun connectDevice(result: ScanResult? = null) {
        val findToResult = result ?: selectedScanItem.value?.scanResult ?: return
        gattServiceDiscovered.value = false

        findViewItem(findToResult)?.let { viewItem ->
            viewItem.scanResult.device.connectGatt(
                getApplication(),
                false,
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
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            viewEvent.postValue(ViewEvent.BleDisconnected)
                            viewItem.connected.postValue(false)
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        super.onServicesDiscovered(gatt, status)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.d(TAG, "service size: ${gatt.services.size}")
                            viewItem.gatt = gatt

                            viewItem.getGattService()?.let { service ->
//                                val characteristic = service.characteristics[Constants.DIST_CMD_CHAR_IDX].apply {
//                                    Log.d(TAG, "CMD_BLE_START: $uuid")
//                                    setValue(Constants.CMD_BLE_START)
//                                    writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//                                }
//                                gatt.writeCharacteristic(characteristic)

                                gatt.readCharacteristic(service.characteristics[Constants.CYCLE_CMD_CHAR_IDX])
                            }

                            gattServiceDiscovered.postValue(true)
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

                        if (characteristic.uuid == Constants.Battery_Characteristic_UUID) {
                            val battery = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
//                            parseBleRawData(viewItem.scanResult, battery)
                        } else {
                            readBattery(gatt)
                            cyclePeriod.postValue(CyclePeriod.convertFromString(value))
                        }
                    }
                })
        }
    }

    private fun readBattery(gatt: BluetoothGatt) {
        try {
            gatt.getService(Constants.Battery_Service_UUID)?.let { service ->
                val characteristic = service.getCharacteristic(Constants.Battery_Characteristic_UUID)
                gatt.readCharacteristic(characteristic).also {
                    Log.d(TAG, "readCharacteristic: $it, ${characteristic.uuid}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "$e")
        }
    }

    fun disConnectGatt() {
        val viewItem = selectedScanItem.value ?: return
        val gatt = viewItem.gatt ?: return
        Log.d(TAG, "disConnectGatt")
        gatt.disconnect()
    }

    fun sendStartCmd() {
        val viewItem = selectedScanItem.value ?: return
        val gatt = viewItem.gatt ?: return
        val gattService = viewItem.getGattService() ?: return
        Log.d(TAG, "sendStartCmd")

        val characteristic = gattService.characteristics[Constants.DIST_CMD_CHAR_IDX].apply {
            setValue(Constants.CMD_BLE_START)
            writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        gatt.writeCharacteristic(characteristic).also {
            Log.d(TAG, "writeCharacteristic: $it, ${characteristic.uuid}")
        }
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
        gatt.writeCharacteristic(characteristic).also {
            Log.d(TAG, "writeCharacteristic: $it, ${characteristic.uuid}")
        }
        gatt.readCharacteristic(characteristic).also {
            Log.d(TAG, "readCharacteristic: $it, ${characteristic.uuid}")
        }

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

    companion object {
        const val TAG = "ActivityViewModel"
        const val BLE_VALUE_STX = 0x40.toByte()
        const val RADIAN_TO_DEGREE: Double = 180 / 3.14159
    }
}