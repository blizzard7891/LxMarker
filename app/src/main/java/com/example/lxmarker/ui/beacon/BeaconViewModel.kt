package com.example.lxmarker.ui.beacon

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.*
import com.example.lxmarker.data.ViewEvent
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import javax.inject.Inject

class BeaconViewModel @Inject constructor(context: Application) : AndroidViewModel(context) {

    val address: MutableLiveData<String> = MutableLiveData("")

    private val _userName: MutableLiveData<String> = MutableLiveData("")
    val userName: LiveData<String> = _userName.map { if (it.isNullOrEmpty()) "이름 없음" else it }

    val advertiseData: LiveData<ByteArray> = MediatorLiveData<ByteArray>().apply {
        addSource(address) { if (canAdvertise()) value = getAdvertiseData() }
        addSource(_userName) { if (canAdvertise()) value = getAdvertiseData() }
    }.distinctUntilChanged()

    val viewEvent: MutableLiveData<ViewEvent> = LiveEvent(LiveEventConfig.PreferFirstObserver)

    private val sharedPref by lazy { getApplication<Application>().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE) }

    fun init() {
        initUserId()
        initUserName()
    }

    fun saveUserName(name: String) {
        sharedPref.edit().putString(KEY_USER_NAME, name).apply()
        initUserName()
    }

    private fun initUserId() {
        address.value = getAndroidId()
    }

    private fun initUserName() {
        _userName.value = sharedPref.getString(KEY_USER_NAME, "").also {
            if (it.isNullOrEmpty()) viewEvent.value = ViewEvent.UserNameSet
        }
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(): String {
        return String
            .format("%16s", Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID))
            .replace(' ', '0')
    }

    private fun canAdvertise(): Boolean {
        return !address.value.isNullOrEmpty() && !_userName.value.isNullOrEmpty()
    }

    private fun getAdvertiseData(): ByteArray {
        val userId = address.value.orEmpty()
        val userName = userName.value.orEmpty()

        val userIdByteArray = userId.toByteArray().copyOf(16)
        val userNameByteArray = userName.toByteArray().copyOf(8)
        return userIdByteArray + userNameByteArray
    }

    private companion object {
        const val TAG = "BeaconViewModel"
        const val PREF_FILE_NAME = "lxMarker"
        const val KEY_USER_NAME = "userName"
    }
}