package com.example.lxmarker.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.databinding.MainFragmentBinding

class MainFragment : Fragment(R.layout.main_fragment) {

    private var binding: MainFragmentBinding? = null
    private val navController: NavController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.apply {
            settingButton.setOnClickListener { navController.navigate(R.id.settingFragment) }

            measureButton.setOnClickListener { navController.navigate(R.id.settingFragment) }

            checkInButton.setOnClickListener { navController.navigate(R.id.checkInFragment) }

            wirelessTagButton.setOnClickListener { navController.navigate(R.id.wirelessTagFragment) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}