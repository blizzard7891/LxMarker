package com.example.lxmarker.ui

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lxmarker.data.CheckIn
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.data.repository.MarkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: MarkerRepository
) : ViewModel() {

    val checkInList: MutableLiveData<List<CheckInItem>> = MutableLiveData()

    fun init() {
        Single.fromCallable { repository.getAllCheckIn() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                checkInList.value = result.map { CheckInItem.Item(it) }.toMutableList<CheckInItem>().apply {
                    add(0, CheckInItem.Top)
                }
            }, {
                Log.e(TAG, "$it")
            })
    }

    fun clearData() {
        Completable.fromAction {
            repository.deleteAllCheckIn()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                init()
                Log.d(TAG, "clearData complete")
            }, {
                Log.e(TAG, "clearData error: $it")
            })
    }

    private companion object {
        const val TAG = "CheckInViewModel"
    }
}