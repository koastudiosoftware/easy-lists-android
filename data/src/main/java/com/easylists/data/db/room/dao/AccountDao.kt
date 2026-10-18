package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlin.time.Clock

@Dao
abstract class AccountDao {

    @Query("""
        UPDATE users
        SET user_id = :newId, email = :email, displayName = :displayName,
            profilePictureUrl = :photoUrl, syncEnabled = 1, modified_timestamp = :now
        WHERE user_id = :oldId
    """)
    protected abstract suspend fun relinkUser(
        oldId: String, newId: String, email: String?, displayName: String?, photoUrl: String?, now: Long
    )

    @Query("UPDATE lists SET owner_id = :newId, is_dirty = 1 WHERE owner_id = :oldId")
    protected abstract suspend fun relinkLists(oldId: String, newId: String)

    @Query("UPDATE categories SET owner_id = :newId, is_dirty = 1 WHERE owner_id = :oldId")
    protected abstract suspend fun relinkCategories(oldId: String, newId: String)

    @Query("UPDATE tags SET owner_id = :newId, is_dirty = 1 WHERE owner_id = :oldId")
    protected abstract suspend fun relinkTags(oldId: String, newId: String)

    @Query("UPDATE list_order SET user_id = :newId, is_dirty = 1 WHERE user_id = :oldId")
    protected abstract suspend fun relinkListOrder(oldId: String, newId: String)

    @Transaction
    open suspend fun relinkLocalUser(
        oldId: String, newId: String, email: String?, displayName: String?, photoUrl: String?,
        now: Long = Clock.System.now().toEpochMilliseconds(),
    ) {
        relinkUser(oldId, newId, email, displayName, photoUrl, now)
        relinkLists(oldId, newId)
        relinkCategories(oldId, newId)
        relinkTags(oldId, newId)
        relinkListOrder(oldId, newId)
    }
}