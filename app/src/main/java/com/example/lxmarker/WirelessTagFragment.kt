package com.example.lxmarker

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.databinding.WirelessTagFragmentBinding
import com.example.lxmarker.dialog.UserSetDialogFragment

class WirelessTagFragment : Fragment(R.layout.wireless_tag_fragment) {

    private var binding: WirelessTagFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_home_icon)
            setNavigationOnClickListener { findNavController().popBackStack() }
            inflateMenu(R.menu.setting_menu)
        }

        binding?.finishButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        binding?.setButton?.setOnClickListener {
            UserSetDialogFragment().show(childFragmentManager, "UserSetDialog")
        }
    }
}