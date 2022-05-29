package com.example.lxmarker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lxmarker.R
import com.example.lxmarker.data.CheckInItem
import com.example.lxmarker.databinding.CheckInItemBinding
import com.example.lxmarker.databinding.CheckInTopItemBinding

class CheckInItemListAdapter :
    ListAdapter<CheckInItem, CheckInItemListAdapter.CheckInViewHolder>(ItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.check_in_top_item -> CheckInViewHolder.Top(
                DataBindingUtil.inflate(inflater, R.layout.check_in_top_item, parent, false)
            )
            else -> CheckInViewHolder.Item(
                DataBindingUtil.inflate(inflater, R.layout.check_in_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {
        if (holder is CheckInViewHolder.Item) {
            holder.bind(getItem(position) as CheckInItem.Item)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).layoutResId

    sealed class CheckInViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        class Top(binding: CheckInTopItemBinding) : CheckInViewHolder(binding.root)
        class Item(private val binding: CheckInItemBinding) : CheckInViewHolder(binding.root) {
            fun bind(item: CheckInItem.Item) {
                binding.item = item
            }
        }
    }

    private object ItemDiffCallback : DiffUtil.ItemCallback<CheckInItem>() {
        override fun areItemsTheSame(oldItem: CheckInItem, newItem: CheckInItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CheckInItem, newItem: CheckInItem): Boolean {
            return oldItem == newItem
        }
    }
}