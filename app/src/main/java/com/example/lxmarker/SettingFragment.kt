package com.example.lxmarker

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.adapter.ScanItemListAdapter
import com.example.lxmarker.data.ScanResult
import com.example.lxmarker.databinding.SettingFragmentBinding

class SettingFragment : Fragment(R.layout.setting_fragment) {

    private var binding: SettingFragmentBinding? = null
    private val adapter by lazy { ScanItemListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.settingToolbar?.apply {
            setNavigationIcon(R.drawable.ic_home_icon)
            setNavigationOnClickListener { findNavController().popBackStack() }
            inflateMenu(R.menu.setting_menu)
        }

        binding?.scanResultListview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@SettingFragment.adapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        adapter.submitList(listOf(ScanResult("test"), ScanResult("test2")))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}