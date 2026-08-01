package com.fhanafi.pitjarus.data.entity

import androidx.room.Entity

@Entity(tableName = "products", primaryKeys = ["id", "storeId"])
data class ProductEntity(
    val id: Int,
    val storeId: Int,
    val barcode: String,
    val name: String,
    val sku: String,
    val size: String,
    val price: Long,
    val available: Boolean,
    val normalPrice: Long?,
    val promoPrice: Long?
)
