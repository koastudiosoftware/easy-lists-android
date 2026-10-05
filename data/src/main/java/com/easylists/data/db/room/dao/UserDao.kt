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

    @Query("UPDATE users SET syncEnabled = :enabled WHERE user_id = :userId")
    suspend fun setSyncEnabled(userId: String, enabled: Boolean)

    @Query("""
        UPDATE users
        SET displayName = :displayName,
            profilePictureUrl = :profilePictureUrl
        WHERE user_id = :userId
    """)
    suspend fun updateProfile(userId: String, displayName: String?, profilePictureUrl: String?)

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteById(userId: String)


    // --- Read ---

    @Query("SELECT user_id FROM users LIMIT 1")
    suspend fun getUserId(): String

    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun observeById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT syncEnabled FROM users WHERE user_id = :userId")
    fun observeSyncEnabled(userId: String): Flow<Boolean?>

    @Query("SELECT * FROM users")
    fun observeAll(): Flow<List<UserEntity>>

}
