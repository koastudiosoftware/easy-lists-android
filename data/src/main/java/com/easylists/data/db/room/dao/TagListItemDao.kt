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
    @Query("SELECT * FROM tag_list_item")
    abstract fun get(): Flow<List<TagListItemEntity>>

    @Transaction
    @Query("SELECT * FROM tag_list_item WHERE list_item_uid = :listItemUid")
    abstract fun get(listItemUid: String): Flow<List<TagListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: TagListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagListItemEntity: List<TagListItemEntity>): List<Long>

    @Query("DELETE FROM tag_list_item WHERE tag_uid = :tagUid")
    abstract suspend fun delete(tagUid: String)

    @Query("DELETE FROM tag_list_item WHERE tag_uid IN (:tagUid)")
    abstract suspend fun delete(tagUid: List<String>)

    @Query("DELETE FROM tag_list_item WHERE list_item_uid = :listItemUid AND tag_uid IN (:tagUidList)")
    abstract suspend fun delete(listItemUid: String, tagUidList: List<String>)

}
