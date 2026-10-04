package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao() {

    @Transaction
    @Query("SELECT * FROM category ORDER BY name ASC")
    abstract fun get(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(categoryEntity: CategoryEntity): Long

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(map: List<CategoryEntity>)

    @Update
    abstract suspend fun update(categoryEntity: CategoryEntity)

    @Query("DELETE FROM category WHERE category_id = :categoryId")
    abstract suspend fun delete(categoryId: String)

    @Query("DELETE FROM category WHERE category_id IN (:categoryId)")
    abstract suspend fun delete(categoryId: List<String>)


}