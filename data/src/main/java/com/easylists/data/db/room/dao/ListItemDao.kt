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

    @Query("""
        UPDATE list_item
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE uid IN (:listItemUids) AND is_deleted = 0
        """)
    protected abstract suspend fun deleteChunk(listItemUids: List<String>, now: Long): Int

    @Transaction
    open suspend fun delete(
        listItemUids: List<String>,
        now: Long = System.currentTimeMillis()
    ): Int = listItemUids.chunked(500).sumOf { deleteChunk(it, now) }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listItemEntity: ListItemEntity): Long

    @Query("UPDATE list_item SET category_uid = NULL WHERE category_uid IN (:categoryUid)")
    abstract suspend fun removeCategory(categoryUid: List<String>)

}
