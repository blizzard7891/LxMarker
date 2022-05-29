package com.example.lxmarker.data

sealed class ViewEvent {
    object Uwb: ViewEvent()
    object Cycle: ViewEvent()
    object CycleChangeComplete: ViewEvent()
}
