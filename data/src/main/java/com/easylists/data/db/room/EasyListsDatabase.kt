package com.easylists.data.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        CategoryEntity::class,
        ListEntity::class,
        ListItemEntity::class,
        ListItemUpdateEntity::class,
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
    ]
)
abstract class EasyListsDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun listDao(): ListDao
    abstract fun listItemDao(): ListItemDao

}
