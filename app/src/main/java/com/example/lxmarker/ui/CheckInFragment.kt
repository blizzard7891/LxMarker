package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.R
import com.example.lxmarker.ui.adapter.CheckInItemListAdapter
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.databinding.CheckInFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class CheckInFragment : Fragment(R.layout.check_in_fragment) {

    private var binding: CheckInFragmentBinding? = null
    private val adapter by lazy { CheckInItemListAdapter() }
    private val checkInViewModel: CheckInViewModel by viewModels()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }
    private val scanner: BluetoothLeScanner? by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private var scanning: Boolean = false
    private var disposable: Disposable? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            super.onScanResult(callbackType, result)
            val deviceName = result.device.name
            Log.d(TAG, "scan result: $deviceName")
            if (deviceName.isNullOrEmpty() || !deviceName.contains("LX"))
                return
            checkInViewModel.setScanResult(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanning = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        initView()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        startScanning()
    }

    override fun onPause() {
        super.onPause()
        if (scanning) stopScanning()
    }

    private fun setObserver() {
        checkInViewModel.checkInList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (!scanning) startScanning()
        }

        checkInViewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            if (event !is ViewEvent.CheckInFound) return@observe

            event.item.let {
                AlertDialog.Builder(context)
                    .setTitle("LX MARKER 감지")
                    .setMessage(
                        """
                        감지 ID [${it.imei}]
                        감지 시간 [${it.time}]
                        가속도 [X:${it.x}, Y:${it.y}, Z:${it.z}]
                    """.trimIndent()
                    )
                    .setPositiveButton("확인", null)
                    .show()
            }
        }
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
            inflateMenu(R.menu.check_in_menu)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.clear_button) checkInViewModel.clearData()
                true
            }
        }

        binding?.checkInListview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@CheckInFragment.adapter
        }
    }

    private fun startScanning() {
        if (!checkInViewModel.scannable) return

        disposable = Completable.fromAction {
            Log.d(TAG, "Starting Scanning")
            scanning = true
            scanner?.startScan(scanCallback)
        }.subscribe(
            { Log.d(TAG, "startScanning complete") },
            { Log.e(TAG, "startScanning error: $it") }
        )
    }

    private fun stopScanning() {
        Log.d(TAG, "Stop scanning")
        scanner?.stopScan(scanCallback)
        scanning = false
    }

    private companion object {
        const val TAG = "CheckInFragment"
    }
}