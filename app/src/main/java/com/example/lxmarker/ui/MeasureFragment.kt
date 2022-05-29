package com.example.lxmarker.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import android.view.animation.Animation.INFINITE
import android.view.animation.LinearInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lxmarker.R
import com.example.lxmarker.databinding.MeasureFragmentBinding

class MeasureFragment : Fragment(R.layout.measure_fragment) {

    private var binding: MeasureFragmentBinding? = null
    private val viewModel: ActivityViewModel by viewModels({ requireActivity() })
    private val measureViewModel: MeasureViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind<MeasureFragmentBinding?>(view)?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MeasureFragment.measureViewModel
        }

        initView()
        viewModel.selectedScanItem.value?.let(measureViewModel::setItem)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}