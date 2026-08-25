package com.remebrit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemStatus { ACTIVE, COMPLETED, SNOOZED, KEPT, EXPIRED, ARCHIVED }

@Entity(tableName = "items")
data class RemebritItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: ItemStatus = ItemStatus.ACTIVE,
    val completedAt: Long? = null
)