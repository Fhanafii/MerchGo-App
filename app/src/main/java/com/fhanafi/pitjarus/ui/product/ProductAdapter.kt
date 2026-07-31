package com.fhanafi.pitjarus.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.ItemProductBinding
import com.fhanafi.pitjarus.ui.model.ProductUiModel

class ProductAdapter : ListAdapter<ProductUiModel, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding) { product ->
            val updatedList = currentList.map {
                if (it.id == product.id) it.copy(isAvailable = !it.isAvailable) else it
            }
            submitList(updatedList)
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onAvailabilityClick: (ProductUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductUiModel) = with(binding) {
            textProductName.text = item.name
            textBarcode.text = root.context.getString(R.string.barcode_format, item.barcode)
            checkAvailable.setOnCheckedChangeListener(null)
            checkAvailable.isChecked = item.isAvailable
            checkAvailable.setOnClickListener { onAvailabilityClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ProductUiModel>() {
        override fun areItemsTheSame(oldItem: ProductUiModel, newItem: ProductUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductUiModel, newItem: ProductUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
