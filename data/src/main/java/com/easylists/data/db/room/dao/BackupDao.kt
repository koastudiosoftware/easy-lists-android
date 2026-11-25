package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity

// All backup and restore queries live here so the existing DAOs stay untouched.
//
// What a backup covers: every list the user owns, shared or not, with its items
// and the categories that belong to it, plus the user's personal categories and
// tags and the order of the lists. People and invites are not part of a snapshot.
//
// A restore only ever replaces what it is told to: unshared lists always, shared
// lists only when the person chose to roll them back, and never a shared list
// that isn't in the backup.
@Dao
abstract class BackupDao {

    //region Reads: live rows owned by the user

    @Query("""
        SELECT * FROM lists
        WHERE owner_id = :ownerId AND is_deleted = 0
        """)
    abstract suspend fun getOwnedLists(ownerId: String): List<ListEntity>

    @Query("""
        SELECT i.*
        FROM list_items i
        JOIN lists l ON l.list_id = i.list_id
        WHERE l.owner_id = :ownerId AND l.is_deleted = 0 AND i.is_deleted = 0
        """)
    abstract suspend fun getOwnedListItems(ownerId: String): List<ListItemEntity>

    // Personal categories only; the ones that belong to a list are read below
    @Query("""
        SELECT * FROM categories
        WHERE owner_id = :ownerId AND is_deleted = 0 AND list_id IS NULL
        """)
    abstract suspend fun getOwnedCategories(ownerId: String): List<CategoryEntity>

    // The categories of the lists the user owns, whoever created them: a shared
    // list's items point at categories its collaborators may have added
    @Query("""
        SELECT c.*
        FROM categories c
        JOIN lists l ON l.list_id = c.list_id
        WHERE l.owner_id = :ownerId AND l.is_deleted = 0 AND c.is_deleted = 0
        """)
    abstract suspend fun getOwnedListCategories(ownerId: String): List<CategoryEntity>

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

    // The order of the lists the user owns.
    @Query("""
        SELECT o.*
        FROM list_order o
        JOIN lists l ON l.list_id = o.list_id
        WHERE o.user_id = :userId AND l.owner_id = :userId
          AND l.is_deleted = 0
        """)
    abstract suspend fun getListOrder(userId: String): List<ListOrderEntity>

    //endregion

    //region Restore: soft-delete what the user currently owns
    // Unshared lists and personal categories only. A shared list is only touched
    // when it is rolled back, through the "OfLists" queries further down.

    @Query("""
        UPDATE categories
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :ownerId AND is_deleted = 0 AND list_id IS NULL
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
          AND list_id IN (
              SELECT list_id FROM lists
              WHERE owner_id = :ownerId AND is_shared = 0
          )
        """)
    abstract suspend fun softDeleteOwnedListItems(ownerId: String, now: Long): Int

    @Query("""
        UPDATE lists
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE owner_id = :ownerId AND is_deleted = 0 AND is_shared = 0
        """)
    abstract suspend fun softDeleteOwnedLists(ownerId: String, now: Long): Int

    // A shared list being rolled back keeps its place: only its items and its
    // categories are cleared, and the snapshot's are written over them.
    @Query("""
        UPDATE list_items
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE is_deleted = 0 AND list_id IN (:listIds)
        """)
    abstract suspend fun softDeleteItemsOfLists(listIds: List<String>, now: Long): Int

    // Whoever created them: only the list's owner may change these on the server
    @Query("""
        UPDATE categories
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE is_deleted = 0 AND list_id IN (:listIds)
        """)
    abstract suspend fun softDeleteCategoriesOfLists(listIds: List<String>, now: Long): Int

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

    // The owner's row for each restored list. The server's rules look the
    // owner up there, so a list without it could never sync.
    @Upsert
    abstract suspend fun upsertMembers(rows: List<ListMemberEntity>)

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

    // Lists that are shared now. A backup made before a list was shared must
    // not overwrite it, which would quietly un-share it.
    @Query("SELECT list_id FROM lists WHERE is_shared = 1")
    abstract suspend fun getSharedListIds(): List<String>

    //endregion

}
