package com.example.lxmarker.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.R
import com.example.lxmarker.ui.adapter.CheckInItemListAdapter
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.databinding.CheckInFragmentBinding

class CheckInFragment : Fragment(R.layout.check_in_fragment) {

    private var binding: CheckInFragmentBinding? = null
    private val adapter by lazy { CheckInItemListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.toolbar?.apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.checkInListview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@CheckInFragment.adapter
        }

        adapter.submitList(listOf(CheckInItem.Top, CheckInItem.Item(), CheckInItem.Item()))
    }
}