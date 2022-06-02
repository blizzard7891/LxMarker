package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.CyclePeriod
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.data.repository.MarkerRepository
import com.example.lxmarker.util.ByteArray.toHexString
import com.example.lxmarker.util.ByteArray.toLittleEndian
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

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

    fun sendContinueCmd() {
        val viewItem = selectedScanItem.value ?: return
        val gatt = viewItem.gatt ?: return
        val gattService = viewItem.getGattService() ?: return
        Log.d(TAG, "sendContinueCmd")

        val characteristic = gattService.characteristics[Constants.DIST_CMD_CHAR_IDX].apply {
            setValue(Constants.CMD_BLE_CONTINUE)
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

    private fun parseBleRawData(result: ScanResult, battery: Int) {
        result.scanRecord
        val byteArray = result.scanRecord!!.bytes
        val stxIndex = byteArray.withIndex().find { (_, byte) -> byte == BLE_VALUE_STX }?.index ?: return

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

        val accX = atan(yAxis / sqrt(xAxis.pow(2) + zAxis.pow(2))) * RADIAN_TO_DEGREE
        val accY = atan((-xAxis) / sqrt(yAxis.pow(2) + zAxis.pow(2))) * RADIAN_TO_DEGREE
        val accZ = atan(zAxis / sqrt((-xAxis).pow(2) + yAxis.pow(2))) * RADIAN_TO_DEGREE

//        Log.d(TAG, "accX: $accX, accY: $accY, accZ: $accZ")

        val entity = CheckIn(
            time = getDateString(),
            markerNum = result.device.address.substring(0, 8) + "\n" + result.device.address.substring(8),
            x = String.format("%.2f", accX),
            y = String.format("%.2f", accY),
            z = String.format("%.2f", accZ)
        )

        Completable.fromAction {
            repository.insertCheckIn(entity)
        }.subscribeOn(Schedulers.io())
            .subscribe({
                Log.d(TAG, "insertCheckIn complete: $entity")
            }, {
                Log.e(TAG, "insertCheckIn error: $it")
            })
    }

    @SuppressLint("SimpleDateFormat")
    private val simpleDateFormat = SimpleDateFormat("yy.MM.dd\nHH:mm:ss")

    private fun getDateString(): String {
        val stamp = System.currentTimeMillis()
        val date = Date(stamp)
        return simpleDateFormat.format(date)
    }

    companion object {
        const val TAG = "ActivityViewModel"
        const val BLE_VALUE_STX = 0x40.toByte()
        const val RADIAN_TO_DEGREE: Double = 180 / 3.14159
    }
}