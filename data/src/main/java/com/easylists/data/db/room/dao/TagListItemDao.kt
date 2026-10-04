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
    @Query("SELECT * FROM tag_list_items")
    abstract fun get(): Flow<List<TagListItemEntity>>

    @Transaction
    @Query("SELECT * FROM tag_list_items WHERE list_item_id = :listItemId")
    abstract fun get(listItemId: String): Flow<List<TagListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: TagListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: List<TagListItemEntity>): List<Long>

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(tagLinks: MutableList<TagListItemEntity>)

    @Query("DELETE FROM tag_list_items WHERE tag_id = :tagId")
    abstract suspend fun delete(tagId: String)

    @Query("DELETE FROM tag_list_items WHERE tag_id IN (:tagIdList)")
    abstract suspend fun delete(tagIdList: List<String>)

    @Query("DELETE FROM tag_list_items WHERE list_item_id = :listItemId AND tag_id IN (:tagIdList)")
    abstract suspend fun delete(listItemId: String, tagIdList: List<String>)

}
