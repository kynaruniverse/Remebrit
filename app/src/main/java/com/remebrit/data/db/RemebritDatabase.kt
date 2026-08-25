package com.remebrit.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.remebrit.data.dao.ItemDao
import com.remebrit.data.entity.ItemStatus
import com.remebrit.data.entity.RemebritItem

class Converters {
    @TypeConverter
    fun fromStatus(status: ItemStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ItemStatus = ItemStatus.valueOf(value)
}

@Database(entities = [RemebritItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RemebritDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile private var INSTANCE: RemebritDatabase? = null

        fun getInstance(context: Context): RemebritDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RemebritDatabase::class.java,
                    "remebrit.db"
                ).build().also { INSTANCE = it }
            }
    }
}