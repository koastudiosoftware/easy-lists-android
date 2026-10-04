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
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListDao() {

    @Transaction
    @Query("SELECT * FROM lists ORDER BY name ASC")
    abstract fun get(): Flow<List<ListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(listEntity: ListEntity): Long

    @Update(entity = ListEntity::class)
    abstract suspend fun updatePartial(listUpdateEntity: ListUpdateEntity)

}
