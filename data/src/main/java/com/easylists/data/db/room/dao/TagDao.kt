package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import com.easylists.data.db.room.models.ListUpdateEntity
import com.easylists.data.db.room.models.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagDao() {

    @Transaction
    @Query("SELECT * FROM tags ORDER BY name ASC")
    abstract fun get(): Flow<List<TagEntity>>

    @Transaction
    @Query("""
        SELECT t.tag_id, t.owner_id, t.name, t.color, t.is_dirty, t.is_deleted, t.created_timestamp, t.modified_timestamp
        FROM tags t
        JOIN tag_list_items ON t.tag_id = tag_list_items.tag_id
        WHERE tag_list_items.list_item_id = :listItemId
        ORDER BY name COLLATE NOCASE ASC
        """)
    abstract fun get(listItemId: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagEntity: TagEntity): Long

    // insertAll is only used for initial database seed data population
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(map: List<TagEntity>)

    @Update
    abstract suspend fun update(tagEntity: TagEntity)

    @Query("DELETE FROM tags WHERE tag_id = :tagId")
    abstract suspend fun delete(tagId: String)

    @Query("DELETE FROM tags WHERE tag_id IN (:tagIdList)")
    abstract suspend fun delete(tagIdList: List<String>)

}
