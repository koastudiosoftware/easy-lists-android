package com.easylists.data.db.room.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListMemberEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.SyncStateEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity

// A tag link plus the owner of its tag. The server copy needs the owner, which the local row lacks.
data class DirtyTagLink(
    @Embedded val link: TagListItemEntity,
    @ColumnInfo(name = "tag_owner_id") val tagOwnerId: String,
)

@Dao
abstract class SyncDao {

    //region Dirty rows (push)
    @Query("SELECT * FROM lists WHERE is_dirty = 1")
    abstract suspend fun getDirtyLists(): List<ListEntity>

    @Query("SELECT * FROM list_members WHERE is_dirty = 1")
    abstract suspend fun getDirtyMembers(): List<ListMemberEntity>

    @Query("SELECT * FROM categories WHERE is_dirty = 1")
    abstract suspend fun getDirtyCategories(): List<CategoryEntity>

    @Query("SELECT * FROM list_items WHERE is_dirty = 1")
    abstract suspend fun getDirtyListItems(): List<ListItemEntity>

    @Query("SELECT * FROM tags WHERE is_dirty = 1")
    abstract suspend fun getDirtyTags(): List<TagEntity>

    @Query("""
        SELECT tli.*, t.owner_id AS tag_owner_id
        FROM tag_list_items tli
        JOIN tags t ON t.tag_id = tli.tag_id
        WHERE tli.is_dirty = 1
    """)
    abstract suspend fun getDirtyTagLinks(): List<DirtyTagLink>

    @Query("SELECT * FROM list_order WHERE is_dirty = 1")
    abstract suspend fun getDirtyListOrder(): List<ListOrderEntity>
    //endregion


    //region Mark clean (push)
    // Each one only clears the flag if the row is unchanged since it was read, so an edit
    // made during the upload stays dirty and goes up on the next sync.
    @Query("UPDATE lists SET is_dirty = 0 WHERE list_id = :id AND modified_timestamp = :modified")
    abstract suspend fun markListClean(id: String, modified: Long)

    @Query("UPDATE list_members SET is_dirty = 0 WHERE list_id = :listId AND user_id = :userId AND modified_timestamp = :modified")
    abstract suspend fun markMemberClean(listId: String, userId: String, modified: Long)

    @Query("UPDATE categories SET is_dirty = 0 WHERE category_id = :id AND modified_timestamp = :modified")
    abstract suspend fun markCategoryClean(id: String, modified: Long)

    @Query("UPDATE list_items SET is_dirty = 0 WHERE list_item_id = :id AND modified_timestamp = :modified")
    abstract suspend fun markListItemClean(id: String, modified: Long)

    @Query("UPDATE tags SET is_dirty = 0 WHERE tag_id = :id AND modified_timestamp = :modified")
    abstract suspend fun markTagClean(id: String, modified: Long)

    @Query("UPDATE tag_list_items SET is_dirty = 0 WHERE tag_list_item_id = :id AND modified_timestamp = :modified")
    abstract suspend fun markTagLinkClean(id: String, modified: Long)

    @Query("UPDATE list_order SET is_dirty = 0 WHERE user_id = :userId AND list_id = :listId AND modified_timestamp = :modified")
    abstract suspend fun markListOrderClean(userId: String, listId: String, modified: Long)
    //endregion


    //region Cursors (pull)
    @Query("SELECT cursor_value FROM sync_state WHERE cursor_key = :cursorKey")
    abstract suspend fun getCursor(cursorKey: String): Long?

    @Upsert
    abstract suspend fun setCursor(state: SyncStateEntity)

    // Per-list cursors ("<kind>:<listId>") go when their list is no longer on the device
    @Query("""
        DELETE FROM sync_state
        WHERE instr(cursor_key, ':') > 0
          AND substr(cursor_key, instr(cursor_key, ':') + 1) NOT IN (SELECT list_id FROM lists)
    """)
    abstract suspend fun pruneListCursors()
    //endregion


    //region Local rows (pull)
    // Only memberships the server already knows. A dirty row hasn't been uploaded yet, and asking
    // the server for a list it has never seen is denied.
    @Query("SELECT list_id FROM list_members WHERE user_id = :userId AND is_deleted = 0 AND is_dirty = 0")
    abstract suspend fun getActiveListIds(userId: String): List<String>

    @Query("SELECT * FROM lists WHERE list_id IN (:ids)")
    abstract suspend fun getListsByIds(ids: List<String>): List<ListEntity>

    @Query("SELECT * FROM list_members WHERE list_id IN (:listIds)")
    abstract suspend fun getMembersOfLists(listIds: List<String>): List<ListMemberEntity>

    @Query("SELECT * FROM categories WHERE category_id IN (:ids)")
    abstract suspend fun getCategoriesByIds(ids: List<String>): List<CategoryEntity>

    @Query("SELECT * FROM list_items WHERE list_item_id IN (:ids)")
    abstract suspend fun getListItemsByIds(ids: List<String>): List<ListItemEntity>

    @Query("SELECT * FROM tags WHERE tag_id IN (:ids)")
    abstract suspend fun getTagsByIds(ids: List<String>): List<TagEntity>

    @Query("SELECT * FROM tag_list_items WHERE tag_list_item_id IN (:ids)")
    abstract suspend fun getTagLinksByIds(ids: List<String>): List<TagListItemEntity>

    @Query("SELECT * FROM list_order WHERE user_id = :userId AND list_id IN (:listIds)")
    abstract suspend fun getListOrderByLists(userId: String, listIds: List<String>): List<ListOrderEntity>

    // Which referenced parents exist, so a pulled row never points at a missing parent
    @Query("SELECT list_id FROM lists WHERE list_id IN (:ids)")
    abstract suspend fun getExistingListIds(ids: List<String>): List<String>

    @Query("SELECT category_id FROM categories WHERE category_id IN (:ids)")
    abstract suspend fun getExistingCategoryIds(ids: List<String>): List<String>

    @Query("SELECT list_item_id FROM list_items WHERE list_item_id IN (:ids)")
    abstract suspend fun getExistingListItemIds(ids: List<String>): List<String>

    @Query("SELECT tag_id FROM tags WHERE tag_id IN (:ids)")
    abstract suspend fun getExistingTagIds(ids: List<String>): List<String>
    //endregion


    //region Apply (pull)
    // @Upsert updates a row in place; @Insert(REPLACE) would delete and re-insert it and the
    // foreign key cascades would wipe the rows that hang off it.
    @Upsert
    abstract suspend fun upsertLists(rows: List<ListEntity>)

    @Upsert
    abstract suspend fun upsertMembers(rows: List<ListMemberEntity>)

    @Upsert
    abstract suspend fun upsertCategories(rows: List<CategoryEntity>)

    @Upsert
    abstract suspend fun upsertListItems(rows: List<ListItemEntity>)

    @Upsert
    abstract suspend fun upsertTags(rows: List<TagEntity>)

    @Upsert
    abstract suspend fun upsertTagLinks(rows: List<TagListItemEntity>)

    @Upsert
    abstract suspend fun upsertListOrder(rows: List<ListOrderEntity>)

    // The photo files of lists that are about to be removed from this device
    @Query("SELECT photo_uri FROM list_items WHERE list_id IN (:listIds) AND photo_uri IS NOT NULL")
    abstract suspend fun getPhotoNames(listIds: List<String>): List<String>

    // A hard delete: items, categories, members and list order go with the list through the cascades
    @Query("DELETE FROM lists WHERE list_id IN (:listIds)")
    abstract suspend fun deleteListsHard(listIds: List<String>)
    //endregion
}