package com.easylists.data.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easylists.data.db.room.dao.AccountDao
import com.easylists.data.db.room.dao.BackupDao
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.db.room.dao.ListMemberDao
import com.easylists.data.db.room.dao.ListOrderDao
import com.easylists.data.db.room.dao.ProviderLinkDao
import com.easylists.data.db.room.dao.ReconcileDao
import com.easylists.data.db.room.dao.ResetDao
import com.easylists.data.db.room.dao.ShareDao
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.db.room.dao.TagListItemDao
import com.easylists.data.db.room.dao.UserDao
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.ProviderLinkEntity
import com.easylists.data.db.room.models.SyncStateEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.data.db.room.models.UserEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        CategoryEntity::class,
        ListEntity::class,
        ListItemEntity::class,
        ListMemberEntity::class,
        ListOrderEntity::class,
        ProviderLinkEntity::class,
        SyncStateEntity::class,
        TagEntity::class,
        TagListItemEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
    ]
)
abstract class EasyListsDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun listDao(): ListDao
    abstract fun listItemDao(): ListItemDao
    abstract fun listMemberDao(): ListMemberDao
    abstract fun listOrderDao(): ListOrderDao
    abstract fun providerLinkDao(): ProviderLinkDao
    abstract fun reconcileDao(): ReconcileDao
    abstract fun shareDao(): ShareDao
    abstract fun syncDao(): SyncDao
    abstract fun tagDao(): TagDao
    abstract fun tagListItemDao(): TagListItemDao
    abstract fun userDao(): UserDao
    abstract fun backupDao(): BackupDao
    abstract fun resetDao(): ResetDao

}
