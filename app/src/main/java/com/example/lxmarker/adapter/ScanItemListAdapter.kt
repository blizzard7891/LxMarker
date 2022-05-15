package com.example.lxmarker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lxmarker.data.ScanResult
import com.example.lxmarker.databinding.ScanItemBinding


class ScanItemListAdapter :
    ListAdapter<ScanResult, ScanItemListAdapter.ItemHolder>(ItemDIffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ScanItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = Unit

    class ItemHolder(binding: ScanItemBinding) : RecyclerView.ViewHolder(binding.root)

    object ItemDIffCallback : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem == newItem
        }
    }
}