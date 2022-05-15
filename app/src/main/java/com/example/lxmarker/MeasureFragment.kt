package com.example.lxmarker

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.databinding.MeasureFragmentBinding

class MeasureFragment : Fragment(R.layout.measure_fragment) {

    private var binding: MeasureFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_home_icon)
            setNavigationOnClickListener { findNavController().popBackStack() }
            inflateMenu(R.menu.setting_menu)
        }

        binding?.finishButton?.setOnClickListener {
            findNavController().popBackStack(R.id.measureFragment, true)
            findNavController().navigate(R.id.settingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}