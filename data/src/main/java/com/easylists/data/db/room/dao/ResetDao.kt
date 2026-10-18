package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class ResetDao {

    @Query("DELETE FROM tag_list_items")
    abstract suspend fun deleteTagLinks()

    @Query("DELETE FROM list_items")
    abstract suspend fun deleteListItems()

    @Query("DELETE FROM list_order")
    abstract suspend fun deleteListOrder()

    @Query("DELETE FROM lists")
    abstract suspend fun deleteLists()

    @Query("DELETE FROM categories")
    abstract suspend fun deleteCategories()

    @Query("DELETE FROM tags")
    abstract suspend fun deleteTags()

    @Query("DELETE FROM provider_links")
    abstract suspend fun deleteProviderLinks()

    @Query("DELETE FROM users")
    abstract suspend fun deleteUsers()

}