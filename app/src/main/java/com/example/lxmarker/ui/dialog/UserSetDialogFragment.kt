package com.example.lxmarker.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.lxmarker.R
import com.example.lxmarker.ui.BeaconViewModel

class UserSetDialogFragment(
    private val screenWidth: Int,
    private val viewModel: BeaconViewModel
) : DialogFragment(R.layout.user_setup_dialog) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userNameEditText = view.findViewById<EditText>(R.id.user_edit_text)
        view.findViewById<Button>(R.id.positive_button)?.apply {
            setOnClickListener {
                viewModel.saveUserName(userNameEditText.text.toString())
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (screenWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}