package com.fhanafi.pitjarus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promos")
data class PromoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeId: Int,
    val productName: String,
    val normalPrice: Long,
    val promoPrice: Long,
    val createdAt: String
)
