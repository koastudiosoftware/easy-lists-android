package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.ListMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListMemberDao {

    @Upsert
    abstract suspend fun upsert(members: List<ListMemberEntity>)

    @Query("""
        SELECT * FROM list_members
        WHERE list_id = :listId AND is_deleted = 0
        ORDER BY created_timestamp ASC
        """)
    abstract fun observeMembers(listId: String): Flow<List<ListMemberEntity>>

    @Query("""
        UPDATE list_members
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_id = :listId AND user_id = :userId AND is_deleted = 0
        """)
    abstract suspend fun softDelete(listId: String, userId: String, now: Long): Int

    // Used by the first-sign-in remap
    @Query("""
        UPDATE list_members
        SET user_id = :newId, display_name = :displayName, is_dirty = 1, modified_timestamp = :now
        WHERE user_id = :oldId
        """)
    abstract suspend fun relinkUser(oldId: String, newId: String, displayName: String?, now: Long)

}
