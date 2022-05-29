package com.example.lxmarker.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.R
import com.example.lxmarker.ui.adapter.ScanItemListAdapter
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.databinding.SettingFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.setting_fragment) {

    private var binding: SettingFragmentBinding? = null
    private val viewModel: ActivityViewModel by viewModels({ requireActivity() })

    private val navController: NavController by lazy { findNavController() }
    private val adapter by lazy { ScanItemListAdapter(viewLifecycleOwner, viewModel) }
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
            viewModel.setScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults")
            viewModel.setScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanning = false
            Toast.makeText(context, "scan failed with error: $errorCode", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_BLE_ENABLE) {
            if (bluetoothAdapter?.isEnabled == true && !scanning) startScanning()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<SettingFragmentBinding>(view)?.apply {
            this@apply.viewModel = this@SettingFragment.viewModel
            this@apply.lifecycleOwner = viewLifecycleOwner
        }

        initView()
        setObserver()

        if (bluetoothAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_BLE_ENABLE)
        } else {
            if (!scanning) startScanning()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        stopScanning()
        disposable?.dispose()
    }

    private fun initView() {
        binding?.settingToolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
            inflateMenu(R.menu.setting_menu)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.scan_button) startScanning()
                true
            }
        }

        binding?.scanResultListview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@SettingFragment.adapter
        }
    }

    private fun setObserver() {
        viewModel.scanResult.observe(viewLifecycleOwner, adapter::submitList)

        viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                ViewEvent.Uwb -> navController.navigate(R.id.measureFragment)
                ViewEvent.Cycle -> navController.navigate(R.id.cycleSettingFragment)
                else -> Unit
            }
        }
    }

    private fun startScanning() {
        Log.d(TAG, "Starting Scanning")
        if (scanning) {
            Toast.makeText(context, R.string.already_scanning, Toast.LENGTH_SHORT).show()
            return
        }

        disposable = Completable.fromAction {
            scanning = true
            scanner?.startScan(scanCallback)

            val toastText = "${getString(R.string.scan_start_toast)}  " +
                    "${TimeUnit.SECONDS.convert(SCAN_PERIOD, TimeUnit.MILLISECONDS)}  " +
                    getString(R.string.seconds)
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }.andThen(Completable.timer(SCAN_PERIOD, TimeUnit.MILLISECONDS))
            .doOnTerminate { stopScanning() }
            .subscribe(
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
        const val TAG = "SettingFragment"
        const val REQUEST_BLE_ENABLE = 123

        const val SCAN_PERIOD = 45000L
    }
}