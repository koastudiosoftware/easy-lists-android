package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListUpdateEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
abstract class ListDao() {

    @Transaction
    @Query("""
        SELECT *
        FROM lists
        WHERE is_deleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """)
    abstract fun get(): Flow<List<ListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listEntity: ListEntity): Long

    @Update(entity = ListEntity::class)
    abstract suspend fun updatePartial(listUpdateEntity: ListUpdateEntity)

    @Query("""
        UPDATE lists
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_id IN (:listIds) AND is_deleted = 0
    """)
    protected abstract suspend fun deleteListsChunk(listIds: List<String>, now: Long): Int

    @Query("""
        UPDATE list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_id IN (:listIds) AND is_deleted = 0
    """)
    protected abstract suspend fun deleteListItemsChunk(listIds: List<String>, now: Long): Int

    @Transaction
    open suspend fun delete(
        listIds: List<String>,
        now: Long = Clock.System.now().toEpochMilliseconds()
    ): Int {
        var rows = listIds.chunked(500).sumOf { deleteListItemsChunk(it, now) }
        rows += listIds.chunked(500).sumOf { deleteListsChunk(it, now) }
        return rows
    }

}
