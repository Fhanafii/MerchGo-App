package com.fhanafi.pitjarus.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.ItemProductBinding
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.utils.BarcodeRenderer

class ProductAdapter(
    private val onAvailabilityChanged: (ProductUiModel, Boolean) -> Unit
) : ListAdapter<ProductUiModel, ProductAdapter.ProductViewHolder>(DiffCallback) {
    private var loadingIds: Set<Int> = emptySet()

    fun submitLoadingIds(ids: Set<Int>) {
        loadingIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding) { product, available ->
            onAvailabilityChanged(product, available)
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id in loadingIds)
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onAvailabilityClick: (ProductUiModel, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductUiModel, loading: Boolean) = with(binding) {
            textProductName.text = item.name
            textBarcode.text = root.context.getString(R.string.barcode_format, item.barcode)
            imageBarcode.setImageBitmap(BarcodeRenderer.render(item.barcode))
            checkAvailable.setOnCheckedChangeListener(null)
            checkAvailable.isChecked = item.isAvailable
            checkAvailable.isEnabled = !loading
            progressAvailability.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
            checkAvailable.setOnClickListener { onAvailabilityClick(item, checkAvailable.isChecked) }
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
