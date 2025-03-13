package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.easylists.data.db.room.models.ListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListItemDao() {

    @Transaction
    @Query("SELECT * FROM list_item ORDER BY name ASC")
    abstract fun get(): Flow<List<ListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(brokerEntity: ListItemEntity): Long

    @Query("DELETE FROM list_item WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

}
