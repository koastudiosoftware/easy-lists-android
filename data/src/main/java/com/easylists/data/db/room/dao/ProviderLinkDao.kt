package com.easylists.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.easylists.data.db.room.models.ProviderLinkEntity
import com.easylists.data.db.room.models.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Assumes:
 *   ProviderLinkEntity(provider, providerUserId, userId) with DB columns provider,
 *   provider_user_id, user_id; PK = (provider, provider_user_id);
 *   FK user_id -> users.user_id (ON DELETE CASCADE), index on user_id.
 * Adjust table/column names to match your entities.
 */
@Dao
abstract class ProviderLinkDao {

    // --- Write ---

    /**
     * Throws SQLiteConstraintException if this provider account is already linked
     * (to any user). That's intentional: a provider account must never end up
     * attached to two users.
     */
    @Insert
    abstract suspend fun insert(link: ProviderLinkEntity)

    @Query("DELETE FROM provider_links WHERE provider = :provider AND provider_user_id = :providerUserId")
    abstract suspend fun delete(provider: String, providerUserId: String)


    // --- Read ---

    /** Sign-in lookup: which link (and therefore which user) does this provider account belong to? */
    @Query("SELECT * FROM provider_links WHERE provider = :provider AND provider_user_id = :providerUserId")
    abstract suspend fun getLink(provider: String, providerUserId: String): ProviderLinkEntity?

    /** Sign-in lookup that goes straight to the user row. */
    @Query("""
        SELECT u.*
        FROM users u
        JOIN provider_links pl ON pl.user_id = u.uid
        WHERE pl.provider = :provider
          AND pl.provider_user_id = :providerUserId
    """)
    abstract suspend fun getUserByProvider(provider: String, providerUserId: String): UserEntity?

    /** For a linked-accounts screen. */
    @Query("SELECT * FROM provider_links WHERE user_id = :userId")
    abstract fun observeLinksForUser(userId: String): Flow<List<ProviderLinkEntity>>

    @Query("SELECT COUNT(*) FROM provider_links WHERE user_id = :userId")
    abstract suspend fun countForUser(userId: String): Int


    // --- Combined ---

    /**
     * Unlinks a provider, but refuses to remove a user's last link so they can't
     * lock themselves out. Returns true if the link was removed.
     */
    @Transaction
    open suspend fun unlinkUnlessLast(userId: String, provider: String, providerUserId: String): Boolean {
        if (countForUser(userId) <= 1) return false
        delete(provider, providerUserId)
        return true
    }

}
