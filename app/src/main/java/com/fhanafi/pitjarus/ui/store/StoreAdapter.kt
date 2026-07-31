package com.fhanafi.pitjarus.ui.store

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhanafi.pitjarus.databinding.ItemStoreBinding
import com.fhanafi.pitjarus.ui.model.StoreUiModel

class StoreAdapter(
    private val onItemClick: (StoreUiModel) -> Unit
) : ListAdapter<StoreUiModel, StoreAdapter.StoreViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = ItemStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StoreViewHolder(
        private val binding: ItemStoreBinding,
        private val onItemClick: (StoreUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StoreUiModel) = with(binding) {
            textStoreName.text = item.name
            textStoreCode.text = item.code
            textStoreAddress.text = item.address
            root.setOnClickListener { onItemClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StoreUiModel>() {
        override fun areItemsTheSame(oldItem: StoreUiModel, newItem: StoreUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StoreUiModel, newItem: StoreUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
