package com.example.lxmarker.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.databinding.CycleSettingFragmentBinding

class CycleSettingFragment : Fragment(R.layout.cycle_setting_fragment) {

    private var binding: CycleSettingFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.doneButton?.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}