package com.example.lxmarker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.lxmarker.data.ScanResultItem
import javax.inject.Inject

class MeasureViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    val scanItem: MutableLiveData<ScanResultItem> = MutableLiveData()

    fun setItem(item: ScanResultItem) {
        scanItem.value = item
    }
}