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

    @Query("DELETE FROM tag_list_item WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

}
