package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
abstract class CategoryDao() {

    @Transaction
    @Query("""
        SELECT *
        FROM categories
        WHERE is_deleted = 0
        ORDER BY name COLLATE NOCASE ASC
        """)
    abstract fun get(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(categoryEntity: CategoryEntity): Long

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(map: List<CategoryEntity>)

    @Update
    abstract suspend fun update(categoryEntity: CategoryEntity)

    @Query("""
        UPDATE categories
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE category_id IN (:categoryIds) AND is_deleted = 0
        """)
    protected abstract suspend fun deleteChunk(categoryIds: List<String>, now: Long): Int

    @Query("""
        UPDATE list_items
        SET category_id = NULL, is_dirty = 1, modified_timestamp = :now
        WHERE category_id IN (:categoryIds) AND is_deleted = 0
        """)
    protected abstract suspend fun removeCategoryFromListItems(categoryIds: List<String>, now: Long): Int

    @Transaction
    open suspend fun delete(
        categoryIds: List<String>,
        now: Long = Clock.System.now().toEpochMilliseconds()
    ): Int {
        var rows = removeCategoryFromListItems(categoryIds, now)
        rows = categoryIds.chunked(500).sumOf { deleteChunk(it, now) }
        return rows
    }

}