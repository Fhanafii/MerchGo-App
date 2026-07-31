package com.fhanafi.pitjarus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val body: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val status: PendingActionStatus = PendingActionStatus.PENDING,
    val lastError: String? = null
)

enum class PendingActionStatus {
    PENDING,
    SYNCING,
    SUCCESS,
    FAILED
}

enum class PendingActionType {
    ATTENDANCE_REPORT,
    PRODUCT_REPORT,
    PROMO_REPORT,
    CREATE_STORE,
    CREATE_AND_ASSIGN_PRODUCT,
    ASSIGN_PRODUCT
}
