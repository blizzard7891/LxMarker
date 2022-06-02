package com.example.lxmarker.ui.measure

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
import android.os.Handler
import android.os.Looper
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
import com.example.lxmarker.ui.ActivityViewModel
import com.hoho.android.usbserial.driver.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MeasureFragment : Fragment(R.layout.measure_fragment) {

    private enum class UsbPermission {
        Unknown, Requested, Granted, Denied
    }

    private var binding: MeasureFragmentBinding? = null
    private val viewModel: ActivityViewModel by viewModels({ requireActivity() })
    private val measureViewModel: MeasureViewModel by viewModels()

    private val usbManager: UsbManager by lazy { requireActivity().getSystemService(Context.USB_SERVICE) as UsbManager }
    private var selectedDriver: UsbSerialDriver? = null
    private var connected: Boolean = false
    private var usbSerialPort: UsbSerialPort? = null

    private var readDisposable: Disposable? = null
    private var cmdDisposable: Disposable? = null
    private var usbPermission = UsbPermission.Unknown
    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    private val permissionReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == INTENT_ACTION_GRANT_USB) {
                    usbPermission = if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) UsbPermission.Granted else UsbPermission.Denied
                    connectSerialPort()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectedScanItem.value?.let(measureViewModel::setItem)
        viewModel.connectDevice()
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

    override fun onDestroy() {
        viewModel.disConnectGatt()
        cmdDisposable?.dispose()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(permissionReceiver, IntentFilter(INTENT_ACTION_GRANT_USB))
        if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted) {
            mainHandler.post(::connectSerialPort)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(permissionReceiver)
        if (connected) disConnectSerialPort()
    }

    private fun setObserver() {
        viewModel.gattServiceDiscovered.observe(viewLifecycleOwner) {
            if (it) {
                cmdDisposable = Observable.interval(2, TimeUnit.SECONDS)
                    .doOnSubscribe { viewModel.sendStartCmd() }
                    .subscribe({
                        viewModel.sendStartCmd()
                    }, {
                        Log.e(TAG, "sendStartCmd error: $it")
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

    private fun connectSerialPort() {
        val table = ProbeTable()
        table.addProduct(0x0000, 0x0000, CdcAcmSerialDriver::class.java)
        val availableDrivers = UsbSerialProber(table).findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.e(TAG, "availableDrivers empty")
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
            Log.e(TAG, "driver is null")
            return
        }
        val device = driver.device
        if (device == null) {
            Log.e(TAG, "device is null")
            return
        }
        // Most devices have just one port(port 0)
        val port = driver.ports[0]

        val usbConnection = usbManager.openDevice(device)
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(device)) {
            usbPermission = UsbPermission.Requested
            Log.e(TAG, "connection is null and had no permission")
            val usbPermissionIntent = PendingIntent.getBroadcast(activity, 0, Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_IMMUTABLE)
            usbManager.requestPermission(device, usbPermissionIntent)
            return
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(device)) {
                Toast.makeText(context, R.string.connection_permission_denied, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "connection failed: permission denied")
            } else {
                Toast.makeText(context, R.string.connection_open_failed, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "connection failed: open failed")
            }
            return
        }

        try {
            port.open(usbConnection)
            port.setParameters(19200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port.dtr = true

            usbSerialPort = port
            connected = true
        } catch (e: IOException) {
            Toast.makeText(context, R.string.connection_open_failed, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "usbSerial error: $e")
        }

        readUsbSerial()
    }

    private fun disConnectSerialPort() {
        Log.i(TAG, "disConnectSerialPort")
        connected = false
        try {
            usbSerialPort?.run {
                dtr = false
                close()
            }
        } catch (e: IOException) {
            usbSerialPort = null
        }
        readDisposable?.dispose()
    }

    private fun readUsbSerial() {
        readDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (!connected) return@subscribe
                val port = usbSerialPort ?: return@subscribe
                readBuffer(port)
            }, {
                Log.e(TAG, "$it")
            })
    }

    private fun readBuffer(port: UsbSerialPort) {
        try {
            val buffer = ByteArray(1024)
            val len = port.read(buffer, READ_WAIT_MILLIS)
            val readBuffer = buffer.copyOf(len)

            if (len != 0) {
//                Log.d(TAG, "read:[$len] ${HexDump.dumpHexString(readBuffer)}")
                measureViewModel.setReadByteArray(readBuffer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "readBuffer error: ${e.message}")
            disConnectSerialPort()
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
        const val READ_WAIT_MILLIS = 500
    }
}