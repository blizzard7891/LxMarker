package com.example.lxmarker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            val deniedPermissions = permissions.filter {
                requireActivity().checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
            }.toTypedArray()

            if (deniedPermissions.isNotEmpty()) {
                requireActivity().finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)

        binding?.apply {
            settingButton.setOnClickListener { navController.navigate(R.id.settingFragment) }

            measureButton.setOnClickListener { navController.navigate(R.id.settingFragment) }

            checkInButton.setOnClickListener { navController.navigate(R.id.checkInFragment) }

            wirelessTagButton.setOnClickListener { navController.navigate(R.id.wirelessTagFragment) }
        }

        val deniedPermissions = permissions.filter {
            requireActivity().checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (deniedPermissions.isNotEmpty()) {
            requireActivity().requestPermissions(deniedPermissions,REQUEST_CODE_PERMISSION)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private companion object {
        const val REQUEST_CODE_PERMISSION = 111
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}