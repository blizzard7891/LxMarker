package com.example.lxmarker.ui.beacon

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.lxmarker.R
import java.util.regex.Pattern

class UserSetDialogFragment(
    private val screenWidth: Int,
    private val viewModel: BeaconViewModel
) : DialogFragment(R.layout.user_setup_dialog) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userNameEditText = view.findViewById<EditText>(R.id.user_edit_text).apply {
            filters = arrayOf(
                InputFilter.LengthFilter(8),
                object : InputFilter {
                    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                        val ps = Pattern.compile("^[a-zA-Z0-9]+$")
                        if (!ps.matcher(source).matches()) return ""
                        return null
                    }
                }
            )
        }
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