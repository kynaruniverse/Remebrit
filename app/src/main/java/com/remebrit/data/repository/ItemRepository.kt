package com.remebrit.data.repository

import com.remebrit.data.dao.ItemDao
import com.remebrit.data.entity.ItemStatus
import com.remebrit.data.entity.RemebritItem
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val dao: ItemDao) {
    fun activeItems(): Flow<List<RemebritItem>> = dao.observeByStatus(ItemStatus.ACTIVE)

    suspend fun capture(content: String) {
        if (content.isNotBlank()) dao.insert(RemebritItem(content = content.trim()))
    }

    suspend fun complete(item: RemebritItem) {
        dao.update(item.copy(status = ItemStatus.COMPLETED, completedAt = System.currentTimeMillis()))
    }
}