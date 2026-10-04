package com.easylists.data.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.db.room.dao.ProviderLinkDao
import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.db.room.dao.TagListItemDao
import com.easylists.data.db.room.dao.UserDao
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import com.easylists.data.db.room.models.ProviderLinkEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.data.db.room.models.UserEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        CategoryEntity::class,
        ListEntity::class,
        ListItemEntity::class,
        ListItemUpdateEntity::class,
        ProviderLinkEntity::class,
        TagEntity::class,
        TagListItemEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = true,
    exportSchema = false,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
    ]
)
abstract class EasyListsDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun listDao(): ListDao
    abstract fun listItemDao(): ListItemDao
    abstract fun providerLinkDao(): ProviderLinkDao
    abstract fun tagDao(): TagDao
    abstract fun tagListItemDao(): TagListItemDao
    abstract fun userDao(): UserDao


}
