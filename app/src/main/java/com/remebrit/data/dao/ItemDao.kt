package com.remebrit.data.dao

import androidx.room.*
import com.remebrit.data.entity.ItemStatus
import com.remebrit.data.entity.RemebritItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: RemebritItem): Long

    @Update
    suspend fun update(item: RemebritItem)

    @Delete
    suspend fun delete(item: RemebritItem)
    
    @Query("SELECT * FROM items")
    suspend fun getAll(): List<RemebritItem>

    @Insert
    suspend fun insertAll(items: List<RemebritItem>)

    @Query("SELECT * FROM items WHERE id = :id")
    fun observeById(id: Long): Flow<RemebritItem?>

    @Query("SELECT * FROM items WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: ItemStatus = ItemStatus.ACTIVE): Flow<List<RemebritItem>>

    @Query("SELECT * FROM items WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<RemebritItem>>
}