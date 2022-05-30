package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import java.net.NetworkInterface
import java.util.*
import javax.inject.Inject

class BeaconViewModel @Inject constructor(context: Application) : AndroidViewModel(context) {

    val address: MutableLiveData<String> = MutableLiveData("00:00:00:00:00:00")

    private val _userName: MutableLiveData<String> = MutableLiveData("")
    val userName: LiveData<String> = _userName.map { if (it.isNullOrEmpty()) "이름 없음" else it }

    private val sharedPref by lazy { getApplication<Application>().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE) }

    fun init() {
        initMacAddress()
        initUserName()
    }

    private fun initUserName() {
        _userName.value = sharedPref.getString(KEY_USER_NAME, "")
    }

    fun saveUserName(name: String) {
        sharedPref.edit().putString(KEY_USER_NAME, name).apply()
        initUserName()
    }

    private fun initMacAddress() {
//        address.value = getMacAddress().also {
//            Log.d(TAG, "mac: $it")
//        }
        address.value = getAndroidId()
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(): String {
        return Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getMacAddress(): String {
        val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (nif in all) {
            Log.d(TAG, "nif name: ${nif.name}")

            if (!nif.name.equals("rmnet_data0", ignoreCase = true)) continue
            Log.d(TAG, "nif2 name: ${nif.name}, ${nif.hardwareAddress}")

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
        const val PREF_FILE_NAME = "lxMarker"
        const val KEY_USER_NAME = "userName"
    }
}