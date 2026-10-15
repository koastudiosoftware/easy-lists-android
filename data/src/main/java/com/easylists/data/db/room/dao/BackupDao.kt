package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity

// All backup and restore queries live here so the existing DAOs stay untouched.
// "Owned" currently means lists.owner_id / tags.owner_id / categories.owner_id = the user.
// When list_members exists, the list queries below are where "owned" becomes "my role is owner".
@Dao
abstract class BackupDao {

    //region Reads: live rows owned by the user

    @Query("SELECT * FROM lists WHERE owner_id = :ownerId AND is_deleted = 0")
    abstract suspend fun getOwnedLists(ownerId: String): List<ListEntity>

    @Query("""
        SELECT i.*
        FROM list_items i
        JOIN lists l ON l.list_id = i.list_id
        WHERE l.owner_id = :ownerId AND l.is_deleted = 0 AND i.is_deleted = 0
        """)
    abstract suspend fun getOwnedListItems(ownerId: String): List<ListItemEntity>

    @Query("SELECT * FROM categories WHERE owner_id = :ownerId AND is_deleted = 0")
    abstract suspend fun getOwnedCategories(ownerId: String): List<CategoryEntity>

    @Query("SELECT * FROM tags WHERE owner_id = :ownerId AND is_deleted = 0")
    abstract suspend fun getOwnedTags(ownerId: String): List<TagEntity>

    // A link is backed up only when the tag is the user's and the item is in a list the user owns.
    @Query("""
        SELECT tli.*
        FROM tag_list_items tli
        JOIN tags t ON t.tag_id = tli.tag_id
        JOIN list_items i ON i.list_item_id = tli.list_item_id
        JOIN lists l ON l.list_id = i.list_id
        WHERE tli.is_deleted = 0
          AND t.owner_id = :ownerId AND t.is_deleted = 0
          AND i.is_deleted = 0
          AND l.owner_id = :ownerId AND l.is_deleted = 0
        """)
    abstract suspend fun getOwnedTagLinks(ownerId: String): List<TagListItemEntity>

    // Only the order of lists the user owns. Their order for shared lists isn't part of the backup.
    @Query("""
        SELECT o.*
        FROM list_order o
        JOIN lists l ON l.list_id = o.list_id
        WHERE o.user_id = :userId AND l.owner_id = :userId AND l.is_deleted = 0
        """)
    abstract suspend fun getListOrder(userId: String): List<ListOrderEntity>

    //endregion

    //region Restore: soft-delete what the user currently owns

    @Query("""
        UPDATE categories
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :ownerId AND is_deleted = 0
        """)
    abstract suspend fun softDeleteOwnedCategories(ownerId: String, now: Long): Int

    @Query("""
        UPDATE tags
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :ownerId AND is_deleted = 0
        """)
    abstract suspend fun softDeleteOwnedTags(ownerId: String, now: Long): Int

    // Same predicate as getOwnedTagLinks, so a restore only touches what a backup would have captured.
    // A collaborator's own tags on the user's items are left alone.
    @Query("""
        UPDATE tag_list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE is_deleted = 0
          AND tag_id IN (SELECT tag_id FROM tags WHERE owner_id = :ownerId)
          AND list_item_id IN (
              SELECT i.list_item_id
              FROM list_items i
              JOIN lists l ON l.list_id = i.list_id
              WHERE l.owner_id = :ownerId
          )
        """)
    abstract suspend fun softDeleteOwnedTagLinks(ownerId: String, now: Long): Int

    @Query("""
        UPDATE list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE is_deleted = 0
          AND list_id IN (SELECT list_id FROM lists WHERE owner_id = :ownerId)
        """)
    abstract suspend fun softDeleteOwnedListItems(ownerId: String, now: Long): Int

    @Query("""
        UPDATE lists
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :ownerId AND is_deleted = 0
        """)
    abstract suspend fun softDeleteOwnedLists(ownerId: String, now: Long): Int

    //endregion

    //region Restore: upsert the backup's rows
    // @Upsert updates a row in place when its key exists. @Insert(REPLACE) would delete and re-insert it,
    // and the foreign key cascades would then hard-delete child rows that should have been tombstoned.

    @Upsert
    abstract suspend fun upsertCategories(rows: List<CategoryEntity>)

    @Upsert
    abstract suspend fun upsertTags(rows: List<TagEntity>)

    @Upsert
    abstract suspend fun upsertLists(rows: List<ListEntity>)

    @Upsert
    abstract suspend fun upsertListItems(rows: List<ListItemEntity>)

    @Upsert
    abstract suspend fun upsertTagLinks(rows: List<TagListItemEntity>)

    @Upsert
    abstract suspend fun upsertListOrder(rows: List<ListOrderEntity>)

    //endregion

    //region Restore: which referenced rows exist (and are live) at this point in the restore

    @Query("SELECT category_id FROM categories WHERE is_deleted = 0")
    abstract suspend fun getLiveCategoryIds(): List<String>

    @Query("SELECT tag_id FROM tags WHERE is_deleted = 0")
    abstract suspend fun getLiveTagIds(): List<String>

    @Query("SELECT list_item_id FROM list_items WHERE is_deleted = 0")
    abstract suspend fun getLiveListItemIds(): List<String>

    //endregion

}