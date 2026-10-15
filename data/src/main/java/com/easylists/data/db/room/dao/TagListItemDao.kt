package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import com.easylists.data.db.room.models.ListUpdateEntity
import com.easylists.data.db.room.models.TagListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagListItemDao() {

    @Transaction
    @Query("SELECT * FROM tag_list_items WHERE is_deleted = 0")
    abstract fun get(): Flow<List<TagListItemEntity>>

    @Transaction
    @Query("SELECT * FROM tag_list_items WHERE list_item_id = :listItemId AND is_deleted = 0")
    abstract fun get(listItemId: String): Flow<List<TagListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: TagListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: List<TagListItemEntity>): List<Long>

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(tagLinks: MutableList<TagListItemEntity>)

    // Links are soft-deleted instead of removed so the deletion can sync to the user's other devices.
    // The delete() functions below keep the signatures the old hard-delete queries had.
    @Query("""
        UPDATE tag_list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE tag_id IN (:tagIds) AND is_deleted = 0
        """)
    protected abstract suspend fun softDeleteForTagsChunk(tagIds: List<String>, now: Long): Int

    @Query("""
        UPDATE tag_list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_item_id = :listItemId AND tag_id IN (:tagIds) AND is_deleted = 0
        """)
    protected abstract suspend fun softDeleteForListItemChunk(
        listItemId: String,
        tagIds: List<String>,
        now: Long
    ): Int

    // Removes every link to one tag
    open suspend fun delete(
        tagId: String,
        now: Long = System.currentTimeMillis()
    ) {
        softDeleteForTagsChunk(listOf(tagId), now)
    }

    // Removes every link to any of the given tags
    @Transaction
    open suspend fun delete(
        tagIdList: List<String>,
        now: Long = System.currentTimeMillis()
    ) {
        tagIdList.chunked(500).forEach { softDeleteForTagsChunk(it, now) }
    }

    // Removes the links between one list item and the given tags
    @Transaction
    open suspend fun delete(
        listItemId: String,
        tagIdList: List<String>,
        now: Long = System.currentTimeMillis()
    ) {
        tagIdList.chunked(500).forEach { softDeleteForListItemChunk(listItemId, it, now) }
    }

}