package com.example.lxmarker

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
        startLoading()
    }

    private fun startLoading() {
        Handler().postDelayed({ finish() }, 1000)
    }
}