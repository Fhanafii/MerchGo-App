package com.fhanafi.pitjarus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: Int,
    val code: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean
)
