package com.example.lxmarker.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation.INFINITE
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.BuildConfig
import com.example.lxmarker.R
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.databinding.MeasureFragmentBinding
import com.example.lxmarker.util.HexDump
import com.hoho.android.usbserial.driver.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class MeasureFragment : Fragment(R.layout.measure_fragment) {

    private var binding: MeasureFragmentBinding? = null
    private val viewModel: ActivityViewModel by viewModels({ requireActivity() })
    private val measureViewModel: MeasureViewModel by viewModels()

    private val usbManager: UsbManager by lazy { requireActivity().getSystemService(Context.USB_SERVICE) as UsbManager }
    private var selectedDriver: UsbSerialDriver? = null
    private var connected: Boolean = false
    private var usbSerialPort: UsbSerialPort? = null

    private var readDisposable: Disposable? = null
    private var cmdDisposable: Disposable? = null

    private val permissionReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == INTENT_ACTION_GRANT_USB) {
                    connect()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectedScanItem.value?.let(measureViewModel::setItem)
        viewModel.connectDevice()
        connect()
    }

    override fun onDestroy() {
        viewModel.disConnectGatt()
        cmdDisposable?.dispose()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(permissionReceiver, IntentFilter(INTENT_ACTION_GRANT_USB))
        readDisposable = read().subscribe({
            if (!connected) return@subscribe
            val port = usbSerialPort ?: return@subscribe

            readBuffer(port)
        }, {
            Log.e(TAG, "$it")
        })
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(permissionReceiver)
        readDisposable?.dispose()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<MeasureFragmentBinding?>(view)?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MeasureFragment.measureViewModel
        }

        initView()
        setObserver()
    }


    private fun setObserver() {
        viewModel.gattServiceDiscovered.observe(viewLifecycleOwner) {
            if (it) {
                cmdDisposable = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribe({
                        viewModel.sendStartCmd()
                    }, {
                        Log.e(TAG, "$it")
                    })
            }
        }

        viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            if (event == ViewEvent.BleDisconnected) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.connection_alert_title)
                    .setMessage(R.string.connection_alert_message)
                    .setPositiveButton(R.string.connection_alert_button) { _, _ ->
                        findNavController().popBackStack()
                    }
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun connect() {
        val table = ProbeTable()
        table.addProduct(0x0000, 0x0000, CdcAcmSerialDriver::class.java)
        val availableDrivers = UsbSerialProber(table).findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "availableDrivers empty")
            return
        }

        for (driver in availableDrivers) {
            val device = driver.device
            Log.d(TAG, "driver: $driver, vendor: ${device.vendorId}, productName: ${device.productName}")
            if (device.vendorId == 0x0000 && device.productId == 0x0000) {
                selectedDriver = driver
                break
            }
        }

        val driver = selectedDriver

        if (driver == null) {
            Log.d(TAG, "driver is null")
            return
        }

        val connection = usbManager.openDevice(driver.device)
        if (connection == null && !usbManager.hasPermission(driver.device)) {
            Log.d(TAG, "connection is null")
            val usbPermissionIntent = PendingIntent.getBroadcast(activity, 0, Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_IMMUTABLE)
            usbManager.requestPermission(driver.device, usbPermissionIntent)
            return
        } else if (connection == null) {
            Toast.makeText(context, R.string.connection_open_failed, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "connection failed: open failed")
            return
        }

        // Most devices have just one port(port 0)
        val port = driver.ports[0]
        try {
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            usbSerialPort = port
            connected = true
        } catch (e: IOException) {
            Toast.makeText(context, R.string.connection_open_failed, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "$e")
        }
    }

    private fun read(): Observable<Long> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
    }

    private fun readBuffer(port: UsbSerialPort) {
        try {
            val buffer = ByteArray(8192)
            val len = port.read(buffer, READ_WAIT_MILLIS)
            val readBuffer = buffer.copyOf(len)

            if (len != 0) {
                Log.d(TAG, "read:[$len] ${HexDump.dumpHexString(readBuffer)}")
                measureViewModel.setReadByteArray(readBuffer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "$e")
        }
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.finishButton?.setOnClickListener {
            findNavController().popBackStack(R.id.measureFragment, true)
            findNavController().navigate(R.id.settingFragment)
        }


        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.3f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.3f, 1f)

        val animator =
            ObjectAnimator.ofPropertyValuesHolder(binding?.circle, alpha, scaleX, scaleY).apply {
                duration = 2400
                interpolator = LinearInterpolator()
                repeatCount = INFINITE
            }

        val animator2 =
            ObjectAnimator.ofPropertyValuesHolder(binding?.circle1, alpha, scaleX, scaleY).apply {
                duration = 2400
                interpolator = LinearInterpolator()
                repeatCount = INFINITE
                startDelay = 800
            }

        val animator3 =
            ObjectAnimator.ofPropertyValuesHolder(binding?.circle2, alpha, scaleX, scaleY).apply {
                duration = 2400
                interpolator = LinearInterpolator()
                repeatCount = INFINITE
                startDelay = 1600
            }

        AnimatorSet().apply {
            playTogether(animator, animator2, animator3)
        }.start()
    }

    private companion object {
        const val TAG = "MeasureFragment"
        const val INTENT_ACTION_GRANT_USB: String = BuildConfig.APPLICATION_ID + ".GRANT_USB"
        const val READ_WAIT_MILLIS = 300
    }
}