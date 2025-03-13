package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.easylists.data.db.room.models.ListEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListDao() {

    @Transaction
    @Query("SELECT * FROM list ORDER BY name ASC")
    abstract fun get(): Flow<List<ListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listEntity: ListEntity): Long

    @Query("DELETE FROM list WHERE uid = :uid")
    abstract suspend fun delete(uid: String)

}