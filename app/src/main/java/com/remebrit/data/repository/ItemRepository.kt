package com.remebrit.data.repository

import com.remebrit.data.dao.ItemDao
import com.remebrit.data.entity.ItemStatus
import com.remebrit.data.entity.RemebritItem
import com.remebrit.domain.parsing.CaptureParser
import com.remebrit.domain.parsing.DateResolver
import com.remebrit.domain.parsing.EntityType
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val dao: ItemDao) {
    fun activeItems(): Flow<List<RemebritItem>> = dao.observeByStatus(ItemStatus.ACTIVE)
    fun observeItem(id: Long): Flow<RemebritItem?> = dao.observeById(id)
    fun search(query: String): Flow<List<RemebritItem>> = dao.search(query)

    suspend fun capture(content: String) {
        if (content.isBlank()) return
        val now = System.currentTimeMillis()
        val dateKeyword = CaptureParser.parse(content).firstOrNull { it.type == EntityType.DATE }
        val relevantAt = dateKeyword?.let { DateResolver.resolve(it.value, now) }
        dao.insert(RemebritItem(content = content.trim(), relevantAt = relevantAt))
    }

    suspend fun complete(item: RemebritItem) {
        dao.update(item.copy(status = ItemStatus.COMPLETED, completedAt = System.currentTimeMillis()))
    }

    suspend fun snooze(item: RemebritItem, until: Long) {
        dao.update(item.copy(status = ItemStatus.SNOOZED, relevantAt = until))
    }

    suspend fun keep(item: RemebritItem) {
        dao.update(item.copy(status = ItemStatus.KEPT))
    }

    suspend fun archive(item: RemebritItem) {
        dao.update(item.copy(status = ItemStatus.ARCHIVED))
    }

    suspend fun delete(item: RemebritItem) {
        dao.delete(item)
    }
}