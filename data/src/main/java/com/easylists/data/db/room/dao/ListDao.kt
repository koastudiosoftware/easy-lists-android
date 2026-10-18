package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.data.db.room.models.ListUpdateEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
abstract class ListDao() {

    @Transaction
    @Query("""
        SELECT l.*
        FROM lists l
        LEFT JOIN list_order o ON o.list_id = l.list_id AND o.user_id = :userId
        WHERE l.is_deleted = 0
        ORDER BY (o.sort_index IS NULL), o.sort_index ASC, l.name COLLATE NOCASE ASC
        """)
    abstract fun get(userId: String): Flow<List<ListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listEntity: ListEntity): Long


    @Upsert
    protected abstract suspend fun upsertMember(member: ListMemberEntity)

    // Inserts a new list together with its owner's membership row
    @Transaction
    open suspend fun insertWithOwner(listEntity: ListEntity): Long {
        val rowId = insert(listEntity)
        upsertMember(
            ListMemberEntity(
                listId = listEntity.listId,
                userId = listEntity.ownerId,
                role = "owner",
                createdTimestamp = listEntity.createdTimestamp,
                modifiedTimestamp = listEntity.modifiedTimestamp,
            )
        )
        return rowId
    }

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

    // Must run before restoreListsChunk: it matches on the list's deletion timestamp,
    // which restoreListsChunk overwrites.
    @Query("""
        UPDATE list_items
        SET is_deleted = 0, is_dirty = 1, modified_timestamp = :now
        WHERE list_id IN (:listIds)
          AND is_deleted = 1
          AND modified_timestamp = (
              SELECT l.modified_timestamp FROM lists l WHERE l.list_id = list_items.list_id
          )
        """)
    protected abstract suspend fun restoreListItemsChunk(listIds: List<String>, now: Long): Int

    @Query("""
        UPDATE lists
        SET is_deleted = 0, is_dirty = 1, modified_timestamp = :now
        WHERE list_id IN (:listIds) AND is_deleted = 1
        """)
    protected abstract suspend fun restoreListsChunk(listIds: List<String>, now: Long): Int

    @Transaction
    open suspend fun restore(
        listIds: List<String>,
        now: Long = Clock.System.now().toEpochMilliseconds()
    ): Int {
        var rows = listIds.chunked(500).sumOf { restoreListItemsChunk(it, now) }
        rows += listIds.chunked(500).sumOf { restoreListsChunk(it, now) }
        return rows
    }

    @Query("""
        UPDATE lists
        SET owner_id = :newId, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :oldId
        """)
    abstract suspend fun relinkOwner(oldId: String, newId: String, now: Long)

    // used during sign-out
    @Query("DELETE FROM lists WHERE owner_id != :userId")
    abstract suspend fun deleteForeignLists(userId: String): Int

}
