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
import com.easylists.data.db.room.models.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagDao() {

    @Transaction
    @Query("SELECT * FROM tag ORDER BY name ASC")
    abstract fun get(): Flow<List<TagEntity>>

    @Transaction
    @Query("""
        SELECT * FROM tag
        JOIN tag_list_item ON tag.uid = tag_list_item.list_item_uid
        WHERE tag_list_item.list_item_uid = :listItemUid
        ORDER BY name ASC
        """)
    abstract fun get(listItemUid: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tagEntity: TagEntity): Long

    @Update
    abstract suspend fun update(tagEntity: TagEntity)

    @Query("DELETE FROM tag WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

    @Query("DELETE FROM tag WHERE uid IN (:uidList)")
    abstract suspend fun delete(uidList: List<String>)

}
