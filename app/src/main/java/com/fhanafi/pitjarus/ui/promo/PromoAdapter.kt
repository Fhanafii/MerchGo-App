package com.fhanafi.pitjarus.ui.promo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.ItemPromoBinding
import com.fhanafi.pitjarus.ui.model.PromoUiModel

class PromoAdapter : ListAdapter<PromoUiModel, PromoAdapter.PromoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PromoViewHolder(
        private val binding: ItemPromoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PromoUiModel) = with(binding) {
            textProductName.text = item.productName
            textPrice.text = root.context.getString(
                R.string.promo_price_format,
                item.normalPrice,
                item.promoPrice
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PromoUiModel>() {
        override fun areItemsTheSame(oldItem: PromoUiModel, newItem: PromoUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PromoUiModel, newItem: PromoUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
