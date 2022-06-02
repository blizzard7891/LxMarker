package com.example.lxmarker.ui.cyclesetting

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.data.CyclePeriod
import com.example.lxmarker.data.ViewEvent
import com.example.lxmarker.databinding.CycleSettingFragmentBinding
import com.example.lxmarker.ui.ActivityViewModel

class CycleSettingFragment : Fragment(R.layout.cycle_setting_fragment) {

    private var binding: CycleSettingFragmentBinding? = null
    private val viewModel: ActivityViewModel by viewModels({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.connectDevice()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        initView()
        initSpinner()
        setObserver()
    }

    override fun onDestroy() {
        viewModel.disConnectGatt()
        super.onDestroy()
    }

    private fun setObserver() {
        viewModel.cyclePeriod.observe(viewLifecycleOwner) { period ->
            binding?.periodText?.text = when (period) {
                CyclePeriod.MONTH -> "월"
                CyclePeriod.DAY -> "일"
                CyclePeriod.HOUR -> "시"
                else -> "설정 안됨"
            }
        }

        viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            if (event == ViewEvent.CycleChangeComplete) {
                Toast.makeText(context, R.string.period_set_complete, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initSpinner() {
        val items = arrayOf("월", "일", "시")
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            items
        )

        binding?.spinner?.apply {
            adapter = arrayAdapter
        }
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.changeButton?.setOnClickListener {
            val cyclePeriod = when (binding?.spinner?.selectedItemPosition) {
                0 -> CyclePeriod.MONTH
                1 -> CyclePeriod.DAY
                else -> CyclePeriod.HOUR
            }
            viewModel.setCycleSetting(cyclePeriod)
        }

        binding?.doneButton?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private companion object {
        const val TAG = "CycleSettingFragment"
    }
}