package com.example.lxmarker.ui.beacon

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.databinding.WirelessTagFragmentBinding
import com.example.lxmarker.util.ByteArray.toHexString
import com.example.lxmarker.util.Constants

@SuppressLint("MissingPermission")
class WirelessTagFragment : Fragment(R.layout.wireless_tag_fragment) {

    private var binding: WirelessTagFragmentBinding? = null
    private val beaconViewModel: BeaconViewModel by viewModels()

    private val bleManager: BluetoothManager by lazy { requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bleAdapter: BluetoothAdapter by lazy { bleManager.adapter.apply { name = "LX_CON" } }
    private val bleAdvertiser: BluetoothLeAdvertiser by lazy { bleAdapter.bluetoothLeAdvertiser }

    private var advertising = false

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "advertise onStartSuccess")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "advertise onStartFailure: $errorCode")
        }
    }

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
        setObserver()
    }

    override fun onPause() {
        super.onPause()
        stopAdvertising()
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.finishButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        binding?.setButton?.setOnClickListener {
            showUserNameSetDialog()
        }
    }

    private fun setObserver() {
        beaconViewModel.advertiseData.observe(viewLifecycleOwner) { data ->
            Log.d(TAG, "advertiseData: [${data.size}] ${data.toHexString()}")
            Log.d(TAG, "advertiseData: ${String(data)}")
            if (advertising) stopAdvertising()
            startAdvertising(data)
        }

        beaconViewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            if (event == ViewEvent.UserNameSet) showUserNameSetDialog()
        }
    }

    private fun startAdvertising(data: ByteArray) {
        advertising = true
        bleAdvertiser.startAdvertising(
            buildAdvertiseSettings(),
            buildAdvertiseData(data),
            advertiseCallback
        )
    }

    private fun stopAdvertising() {
        advertising = false
        bleAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    private fun buildAdvertiseData(data: ByteArray): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(true)
//            .addServiceUuid(Constants.Service_UUID)
            .addServiceData(Constants.Advertise_Data1_UUID, data)
            .build()
    }

    private fun getScreenSizePx(): Point {
        val wm = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        return Point().apply {
            display.getSize(this)
        }
    }

    private fun showUserNameSetDialog() {
        val screenSize = getScreenSizePx()
        UserSetDialogFragment(screenSize.x, beaconViewModel).show(childFragmentManager, "UserSetDialog")
    }

    private companion object {
        const val TAG = "WirelessTagFragment"
    }
}