package com.example.lxmarker.ui

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.SplashActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment_container) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPLASH_REQUEST_CODE) {
            setContentView(R.layout.activity_main)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashIntent = Intent().apply {
            component = ComponentName(applicationContext, SplashActivity::class.java)
        }
        startActivityForResult(splashIntent, SPLASH_REQUEST_CODE)
    }

    private companion object {
        const val SPLASH_REQUEST_CODE = 111
    }
}