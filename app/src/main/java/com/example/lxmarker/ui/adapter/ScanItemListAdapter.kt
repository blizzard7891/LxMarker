package com.example.lxmarker.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lxmarker.data.ScanResultItem
import com.example.lxmarker.databinding.ScanItemBinding
import com.example.lxmarker.ui.SettingViewModel


class ScanItemListAdapter(
    private val owner: LifecycleOwner,
    private val viewModel: SettingViewModel
) : ListAdapter<ScanResultItem, ScanItemListAdapter.ItemHolder>(ItemDIffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ScanItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                lifecycleOwner = owner
                viewModel = this@ScanItemListAdapter.viewModel
            }
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemHolder(private val binding: ScanItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScanResultItem) {
            binding.item = item
        }
    }

    object ItemDIffCallback : DiffUtil.ItemCallback<ScanResultItem>() {
        override fun areItemsTheSame(oldItem: ScanResultItem, newItem: ScanResultItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ScanResultItem, newItem: ScanResultItem): Boolean {
            return oldItem == newItem
        }
    }
}