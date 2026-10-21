package com.easylists.data.repositories

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.db.room.dao.ListMemberDao
import com.easylists.data.db.room.dao.ListOrderDao
import com.easylists.data.db.room.dao.ReconcileDao
import com.easylists.data.db.room.dao.ResetDao
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.db.room.dao.UserDao
import com.easylists.data.db.room.models.UserEntity
import com.easylists.domain.models.AuthAccount
import com.easylists.domain.models.LinkResult
import com.easylists.domain.repositories.ImageRepository
import com.easylists.domain.repositories.SessionRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class SessionRepositoryImpl @Inject constructor(
    private val db: EasyListsDatabase,
    private val userDao: UserDao,
    private val listDao: ListDao,
    private val listItemDao: ListItemDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val listOrderDao: ListOrderDao,
    private val resetDao: ResetDao,
    private val syncDao: SyncDao,
    private val listMemberDao: ListMemberDao,
    private val imageRepository: ImageRepository,
    private val reconcileDao: ReconcileDao,
    private val firestore: FirebaseFirestore,
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
                // Decided before anything is remapped: the remap bumps every list's modified time,
                // which would make an untouched sample list look edited.
                val dropSamples = accountHasLists(account.uid)
                val now = Clock.System.now().toEpochMilliseconds()
                db.withTransaction {
                    if (dropSamples) removeUntouchedSampleData()
                    userDao.relink(localId, account.uid, account.email, account.displayName, account.photoUrl, now)
                    listDao.relinkOwner(localId, account.uid, now)
                    categoryDao.relinkOwner(localId, account.uid, now)
                    tagDao.relinkOwner(localId, account.uid, now)
                    listOrderDao.relinkUser(localId, account.uid, now)
                    listMemberDao.relinkUser(localId, account.uid, account.displayName, now)
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
            resetDao.deleteSyncState()
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


    override suspend fun removeSharedListsFromDevice() {
        val userId = getUserId()
        val photoNames = listItemDao.getPhotoNamesOfForeignLists(userId)
        listDao.deleteForeignLists(userId)
        syncDao.pruneListCursors()
        imageRepository.deletePhotos(photoNames)
    }

    // Does this account already have an active list? On any failure the answer is "no", so
    // nothing is dropped and the worst case is some duplicate sample lists.
    private suspend fun accountHasLists(uid: String): Boolean = try {
        !firestore.collection("list_members")
            .whereEqualTo("user_id", uid)
            .whereEqualTo("is_deleted", false)
            .limit(1)
            .get().await()
            .isEmpty
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Arbor.e("Could not check whether the account already has lists", e)
        false
    }

    // Sample lists the user never touched, then the sample categories and tags nothing uses
    // any more. They were never uploaded, so they are deleted outright.
    private suspend fun removeUntouchedSampleData() {
        val untouched = reconcileDao.getUntouchedSeedListIds()
        if (untouched.isNotEmpty()) reconcileDao.deleteLists(untouched)
        reconcileDao.deleteUnusedSeedCategories()
        reconcileDao.deleteUnusedSeedTags()
    }

}
