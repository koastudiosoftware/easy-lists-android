package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.easylists.data.db.room.models.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // --- Write ---

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET syncEnabled = :enabled WHERE uid = :userId")
    suspend fun setSyncEnabled(userId: String, enabled: Boolean)

    @Query("""
        UPDATE users
        SET displayName = :displayName,
            profilePictureUrl = :profilePictureUrl
        WHERE uid = :userId
    """)
    suspend fun updateProfile(userId: String, displayName: String?, profilePictureUrl: String?)

    @Query("DELETE FROM users WHERE uid = :userId")
    suspend fun deleteById(userId: String)


    // --- Read ---

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun observeById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT syncEnabled FROM users WHERE uid = :userId")
    fun observeSyncEnabled(userId: String): Flow<Boolean?>

    @Query("SELECT * FROM users")
    fun observeAll(): Flow<List<UserEntity>>

}
