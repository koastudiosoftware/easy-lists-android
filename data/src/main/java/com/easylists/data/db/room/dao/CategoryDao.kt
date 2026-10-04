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

    @Update
    abstract suspend fun update(categoryEntity: CategoryEntity)

    @Query("DELETE FROM category WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

    @Query("DELETE FROM category WHERE uid IN (:uid)")
    abstract suspend fun delete(uid: List<String>)

}