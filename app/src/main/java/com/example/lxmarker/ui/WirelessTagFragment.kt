package com.example.lxmarker.ui

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.databinding.WirelessTagFragmentBinding
import com.example.lxmarker.ui.dialog.UserSetDialogFragment

class WirelessTagFragment : Fragment(R.layout.wireless_tag_fragment) {

    private var binding: WirelessTagFragmentBinding? = null
    private val beaconViewModel: BeaconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<WirelessTagFragmentBinding?>(view)?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = beaconViewModel
        }

        initView()
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.finishButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
    }
}