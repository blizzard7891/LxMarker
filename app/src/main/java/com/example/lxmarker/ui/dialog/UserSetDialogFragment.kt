package com.example.lxmarker.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.lxmarker.R

class UserSetDialogFragment(private val screenWidth: Int) :
    DialogFragment(R.layout.user_setup_dialog) {

    override fun onResume() {
        super.onResume()

        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (screenWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}