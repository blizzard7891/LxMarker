package com.example.lxmarker.data

import androidx.annotation.LayoutRes
import com.example.lxmarker.R

sealed class CheckInItem(@LayoutRes val layoutResId: Int) {
    object Top : CheckInItem(R.layout.check_in_top_item)
    class Item(val checkIn: CheckIn) : CheckInItem(R.layout.check_in_item)
}