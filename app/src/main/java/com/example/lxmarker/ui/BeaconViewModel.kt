package com.example.lxmarker.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.net.NetworkInterface
import java.util.*
import javax.inject.Inject

class BeaconViewModel @Inject constructor() : ViewModel() {

    val address: MutableLiveData<String> = MutableLiveData()

    fun init() {
        setMacAddress()
    }

    private fun setMacAddress() {
        address.value = getMacAddress()
    }

    private fun getMacAddress(): String {

            val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes: ByteArray = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        
        return ""
    }

    private companion object {
        const val TAG = "BeaconViewModel"
    }
}