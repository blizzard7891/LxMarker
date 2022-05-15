package com.example.lxmarker

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.adapter.CheckInItemListAdapter
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.databinding.CheckInFragmentBinding

class CheckInFragment : Fragment(R.layout.check_in_fragment) {

    private var binding: CheckInFragmentBinding? = null
    private val adapter by lazy { CheckInItemListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_home_icon)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding?.checkInListview?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@CheckInFragment.adapter
        }

        adapter.submitList(listOf(CheckInItem.Top, CheckInItem.Item(), CheckInItem.Item()))
    }
}