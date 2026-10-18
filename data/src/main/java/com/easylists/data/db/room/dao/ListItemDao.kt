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
    @Query("""
        SELECT *
        FROM list_items
        WHERE is_deleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """)
    abstract fun get(): Flow<List<ListItemEntity>>

    @Transaction
    @Query("""
        SELECT *
        FROM list_items
        WHERE list_id = :listId AND is_deleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """)
    abstract fun get(listId: String): Flow<List<ListItemEntity>>

    @Update(entity = ListItemEntity::class)
    abstract suspend fun updatePartial(listItemUpdateEntity: ListItemUpdateEntity)

    @Query("""
        UPDATE list_items
        SET crossed_off = 0, is_dirty = 1, modified_timestamp = :now
        WHERE list_id = :listId AND crossed_off = 1 AND is_deleted = 0
        """)
    abstract suspend fun restoreCrossedOff(listId: String, now: Long): Int

    @Query("""
        UPDATE list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_item_id IN (:listItemIds) AND is_deleted = 0
        """)
    protected abstract suspend fun deleteChunk(listItemIds: List<String>, now: Long): Int

    @Transaction
    open suspend fun delete(
        listItemIds: List<String>,
        now: Long = System.currentTimeMillis()
    ): Int = listItemIds.chunked(500).sumOf { deleteChunk(it, now) }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listItemEntity: ListItemEntity): Long

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(map: List<ListItemEntity>)

    @Query("UPDATE list_items SET category_id = NULL WHERE category_id IN (:categoryIdList)")
    abstract suspend fun removeCategory(categoryIdList: List<String>)

    @Query("""
        UPDATE list_items
        SET is_deleted = 0, is_dirty = 1, modified_timestamp = :now
        WHERE list_item_id IN (:listItemIds) AND is_deleted = 1
        """)
    protected abstract suspend fun undeleteChunk(listItemIds: List<String>, now: Long): Int

    @Transaction
    open suspend fun undelete(
        listItemIds: List<String>,
        now: Long = System.currentTimeMillis()
    ): Int = listItemIds.chunked(500).sumOf { undeleteChunk(it, now) }


    // used during sign-out
    @Query("""
        SELECT li.photo_uri
        FROM list_items li
        JOIN lists l ON l.list_id = li.list_id
        WHERE l.owner_id != :userId AND li.photo_uri IS NOT NULL
        """)
    abstract suspend fun getPhotoNamesOfForeignLists(userId: String): List<String>

}
