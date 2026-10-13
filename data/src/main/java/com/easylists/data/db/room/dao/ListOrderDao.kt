package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.easylists.data.db.room.models.ListOrderEntity
import kotlin.time.Clock

@Dao
abstract class ListOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsert(rows: List<ListOrderEntity>)

    // listIds must be the complete list in its new display order.
    // Writing every row keeps indexes contiguous and places any unplaced lists.
    @Transaction
    open suspend fun reorder(
        userId: String,
        listIds: List<String>,
        now: Long = Clock.System.now().toEpochMilliseconds(),
    ) {
        upsert(
            listIds.mapIndexed { index, id ->
                ListOrderEntity(
                    userId = userId,
                    listId = id,
                    sortIndex = index,
                    isDirty = true,
                    modifiedTimestamp = now,
                )
            }
        )
    }
}