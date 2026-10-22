package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.easylists.data.db.room.models.CategoryEntity

// Queries for turning a list into a shared one (see ShareRepositoryImpl)
@Dao
abstract class ShareDao {

    // Who owns the list; null if it doesn't exist on this device
    @Query("SELECT owner_id FROM lists WHERE list_id = :listId")
    abstract suspend fun getOwnerId(listId: String): String?

    // The personal categories the list's live items use. Categories that already belong to a list are left alone.
    @Query("""
        SELECT DISTINCT c.*
        FROM categories c
        JOIN list_items i ON i.category_id = c.category_id
        WHERE i.list_id = :listId
          AND i.is_deleted = 0
          AND c.list_id IS NULL
          AND c.is_deleted = 0
    """)
    abstract suspend fun getPersonalCategoriesUsedBy(listId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertCategory(category: CategoryEntity)

    // Every item of the list, deleted ones included, so an item brought back by undo
    // never points at a personal category again
    @Query("""
        UPDATE list_items
        SET category_id = :to, is_dirty = 1, modified_timestamp = :now
        WHERE list_id = :listId AND category_id = :from
    """)
    abstract suspend fun repointItems(listId: String, from: String, to: String, now: Long): Int

    // For the debug rows: the first live list this user owns
    @Query("SELECT list_id FROM lists WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY created_timestamp LIMIT 1")
    abstract suspend fun getFirstOwnedListId(ownerId: String): String?
}