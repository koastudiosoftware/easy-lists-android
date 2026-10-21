package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity

// Queries for merging duplicate personal categories and tags by name (see SyncReconciler).
// Every change marks the row dirty and bumps modified_timestamp so it syncs.
@Dao
abstract class ReconcileDao {

    //region Categories
    // Includes soft-deleted rows, because items can still point at a duplicate that was already tombstoned
    @Query("SELECT * FROM categories WHERE owner_id = :ownerId AND list_id IS NULL")
    abstract suspend fun getPersonalCategories(ownerId: String): List<CategoryEntity>

    @Query("""
        UPDATE list_items
        SET category_id = :to, is_dirty = 1, modified_timestamp = :now
        WHERE category_id = :from
    """)
    abstract suspend fun repointItems(from: String, to: String, now: Long): Int

    @Query("""
        UPDATE categories
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE category_id = :id AND is_deleted = 0
    """)
    abstract suspend fun softDeleteCategory(id: String, now: Long): Int
    //endregion


    //region Tags
    @Query("SELECT * FROM tags WHERE owner_id = :ownerId")
    abstract suspend fun getTagsOf(ownerId: String): List<TagEntity>

    @Query("SELECT * FROM tag_list_items WHERE tag_id = :tagId AND is_deleted = 0")
    abstract suspend fun getLiveLinksOfTag(tagId: String): List<TagListItemEntity>

    @Query("SELECT list_item_id FROM tag_list_items WHERE tag_id = :tagId AND is_deleted = 0")
    abstract suspend fun getLinkedItemIds(tagId: String): List<String>

    @Query("""
        UPDATE tag_list_items
        SET tag_id = :to, is_dirty = 1, modified_timestamp = :now
        WHERE tag_list_item_id = :id
    """)
    abstract suspend fun retargetLink(id: String, to: String, now: Long): Int

    @Query("""
        UPDATE tag_list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE tag_list_item_id = :id AND is_deleted = 0
    """)
    abstract suspend fun softDeleteLink(id: String, now: Long): Int

    @Query("""
        UPDATE tags
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE tag_id = :id AND is_deleted = 0
    """)
    abstract suspend fun softDeleteTag(id: String, now: Long): Int
    //endregion


    //region Untouched seed data
    // The seed callback records the one timestamp it gave every sample row ("seed_timestamp").
    // A sample row is untouched when it still has that timestamp as both created and modified,
    // has never been uploaded (dirty), and nothing was added to it or changed under it.

    // Sample lists the user hasn't touched: not edited, no items added, edited or deleted, no tags changed
    @Query("""
        SELECT l.list_id
        FROM lists l
        WHERE l.is_dirty = 1
          AND l.is_deleted = 0
          AND l.created_timestamp = (SELECT cursor_value FROM sync_state WHERE cursor_key = 'seed_timestamp')
          AND l.modified_timestamp = l.created_timestamp
          AND NOT EXISTS (
              SELECT 1 FROM list_items i
              WHERE i.list_id = l.list_id
                AND (i.created_timestamp != l.created_timestamp OR i.modified_timestamp != l.created_timestamp)
          )
          AND NOT EXISTS (
              SELECT 1
              FROM tag_list_items t
              JOIN list_items i ON i.list_item_id = t.list_item_id
              WHERE i.list_id = l.list_id
                AND (t.created_timestamp != l.created_timestamp
                     OR t.modified_timestamp != l.created_timestamp
                     OR t.is_deleted = 1)
          )
    """)
    abstract suspend fun getUntouchedSeedListIds(): List<String>

    // A hard delete: these rows were never uploaded, so there is nothing to tombstone.
    // The items and tag links go with the list through the cascades.
    @Query("DELETE FROM lists WHERE list_id IN (:listIds)")
    abstract suspend fun deleteLists(listIds: List<String>)

    // Sample categories that nothing uses any more, once the sample lists are gone
    @Query("""
        DELETE FROM categories
        WHERE is_dirty = 1
          AND is_deleted = 0
          AND list_id IS NULL
          AND created_timestamp = (SELECT cursor_value FROM sync_state WHERE cursor_key = 'seed_timestamp')
          AND modified_timestamp = created_timestamp
          AND NOT EXISTS (SELECT 1 FROM list_items i WHERE i.category_id = categories.category_id)
    """)
    abstract suspend fun deleteUnusedSeedCategories(): Int

    // Sample tags that nothing uses any more
    @Query("""
        DELETE FROM tags
        WHERE is_dirty = 1
          AND is_deleted = 0
          AND created_timestamp = (SELECT cursor_value FROM sync_state WHERE cursor_key = 'seed_timestamp')
          AND modified_timestamp = created_timestamp
          AND NOT EXISTS (SELECT 1 FROM tag_list_items t WHERE t.tag_id = tags.tag_id)
    """)
    abstract suspend fun deleteUnusedSeedTags(): Int
    //endregion

}
