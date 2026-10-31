package com.easylists.data.db.room.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.ListMemberEntity
import kotlinx.coroutines.flow.Flow

// One person on a list. is_you compares with the signed-in user.
data class MemberRow(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "display_name") val displayName: String?,
    val role: String,
    @ColumnInfo(name = "is_you") val isYou: Boolean,
)

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

    // The people on a list, the owner first. The users table holds the one
    // signed-in user, so no user ID needs to be passed in.
    @Query("""
        SELECT m.user_id AS user_id,
               m.display_name AS display_name,
               m.role AS role,
               (m.user_id = u.user_id) AS is_you
        FROM list_members m
        CROSS JOIN users u
        WHERE m.list_id = :listId AND m.is_deleted = 0
        ORDER BY (m.role = 'owner') DESC, m.created_timestamp ASC
        """)
    abstract fun observeRoster(listId: String): Flow<List<MemberRow>>

    // Never the owner's own row
    @Query("""
        UPDATE list_members
        SET role = :role, is_dirty = 1, modified_timestamp = :now
        WHERE list_id = :listId AND user_id = :userId
          AND role != 'owner' AND is_deleted = 0
        """)
    abstract suspend fun setRole(
        listId: String,
        userId: String,
        role: String,
        now: Long,
    ): Int

    @Query("""
        UPDATE list_members
        SET is_deleted = 1, is_dirty = 1, modified_timestamp = :now
        WHERE list_id = :listId AND user_id = :userId
          AND role != 'owner' AND is_deleted = 0
        """)
    abstract suspend fun removeMember(
        listId: String,
        userId: String,
        now: Long,
    ): Int
}
