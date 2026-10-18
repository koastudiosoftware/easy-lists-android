package com.easylists.data.repositories

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListOrderDao
import com.easylists.data.db.room.dao.ResetDao
import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.db.room.dao.UserDao
import com.easylists.data.db.room.models.UserEntity
import com.easylists.domain.models.AuthAccount
import com.easylists.domain.models.LinkResult
import com.easylists.domain.repositories.SessionRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class SessionRepositoryImpl @Inject constructor(
    private val db: EasyListsDatabase,
    private val userDao: UserDao,
    private val listDao: ListDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val listOrderDao: ListOrderDao,
    private val resetDao: ResetDao,
) : SessionRepository {

    override suspend fun getUserId(): String = withTimeout(5.seconds) {
        userDao.observeUserId().filterNotNull().first()
    }

    override suspend fun linkAccount(account: AuthAccount): LinkResult {
        val localId = getUserId()
        val local = userDao.getById(localId) ?: error("Local user row is missing")
        return when {
            localId == account.uid -> {
                userDao.updateProfile(localId, account.displayName, account.photoUrl)
                LinkResult.AlreadyLinked
            }
            !local.syncEnabled -> {
                val now = Clock.System.now().toEpochMilliseconds()
                db.withTransaction {
                    userDao.relink(localId, account.uid, account.email, account.displayName, account.photoUrl, now)
                    listDao.relinkOwner(localId, account.uid, now)
                    categoryDao.relinkOwner(localId, account.uid, now)
                    tagDao.relinkOwner(localId, account.uid, now)
                    listOrderDao.relinkUser(localId, account.uid, now)
                }
                LinkResult.Linked
            }
            else -> LinkResult.SwitchRequired
        }
    }


    override suspend fun switchAccount(account: AuthAccount) {
        db.withTransaction {
            resetDao.deleteTagLinks()
            resetDao.deleteListItems()
            resetDao.deleteListOrder()
            resetDao.deleteLists()
            resetDao.deleteCategories()
            resetDao.deleteTags()
            resetDao.deleteProviderLinks()
            resetDao.deleteUsers()
            userDao.upsert(
                UserEntity(
                    userId = account.uid,
                    email = account.email,
                    displayName = account.displayName,
                    profilePictureUrl = account.photoUrl,
                    syncEnabled = true,
                )
            )
        }
    }
}
