package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListItemDao() {

    @Transaction
    @Query("SELECT * FROM list_item ORDER BY name ASC")
    abstract fun get(): Flow<List<ListItemEntity>>

    @Transaction
    @Query("SELECT * FROM list_item WHERE list_uid = :listUid ORDER BY name ASC")
    abstract fun get(listUid: String): Flow<List<ListItemEntity>>

    @Update(entity = ListItemEntity::class)
    abstract suspend fun updatePartial(listItemUpdateEntity: ListItemUpdateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listItemEntity: ListItemEntity): Long

    @Query("DELETE FROM list_item WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

}
