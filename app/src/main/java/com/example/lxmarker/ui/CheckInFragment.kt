package com.example.lxmarker.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lxmarker.R
import com.example.lxmarker.ui.adapter.CheckInItemListAdapter
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.databinding.CheckInFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckInFragment : Fragment(R.layout.check_in_fragment) {

    private var binding: CheckInFragmentBinding? = null
    private val adapter by lazy { CheckInItemListAdapter() }
    private val checkInViewModel: CheckInViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)

        checkInViewModel.init()
        initView()
        setObserver()
    }

    private fun setObserver() {
        checkInViewModel.checkInList.observe(viewLifecycleOwner, adapter::submitList)
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
}