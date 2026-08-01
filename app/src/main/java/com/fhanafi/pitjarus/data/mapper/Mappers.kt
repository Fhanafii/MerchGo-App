package com.fhanafi.pitjarus.data.mapper

import com.fhanafi.pitjarus.data.entity.ProductEntity
import com.fhanafi.pitjarus.data.entity.PromoEntity
import com.fhanafi.pitjarus.data.entity.StoreEntity
import com.fhanafi.pitjarus.data.model.ProductDto
import com.fhanafi.pitjarus.data.model.StoreDto
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.ui.model.PromoUiModel
import com.fhanafi.pitjarus.ui.model.StoreUiModel
import java.text.NumberFormat
import java.util.Locale

fun StoreDto.toEntity() = StoreEntity(
    id = id,
    code = code,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    isActive = isActive
)

fun StoreEntity.toUiModel() = StoreUiModel(
    id = id,
    name = name,
    code = code,
    address = address
)

fun ProductDto.toEntity(storeId: Int) = ProductEntity(
    id = id,
    storeId = storeId,
    barcode = barcode,
    name = name,
    sku = sku,
    size = size,
    price = price,
    available = available ?: true,
    normalPrice = normalPrice,
    promoPrice = promoPrice
)

fun ProductEntity.toUiModel() = ProductUiModel(
    id = id,
    name = name,
    barcode = barcode,
    isAvailable = available
)

fun PromoEntity.toUiModel() = PromoUiModel(
    id = id,
    productName = productName,
    normalPrice = normalPrice.toRupiah(),
    promoPrice = promoPrice.toRupiah()
)

private fun Long.toRupiah(): String {
    return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(this)
}
