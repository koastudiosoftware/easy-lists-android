package com.easylists.data.repositories

import com.easylists.data.db.room.dao.SyncDao
import com.easylists.data.db.room.models.SyncStateEntity
import com.easylists.data.sync.SyncDownloader
import com.easylists.data.sync.SyncReconciler
import com.easylists.data.sync.SyncUploader
import com.easylists.domain.models.SyncSummary
import com.easylists.domain.repositories.SyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val downloader: SyncDownloader,
    private val reconciler: SyncReconciler,
    private val uploader: SyncUploader,
    private val syncDao: SyncDao,
) : SyncRepository {

    // Only one sync at a time, however it was started
    private val mutex = Mutex()

    override suspend fun sync(minIntervalSeconds: Long): SyncSummary = mutex.withLock {
        if (minIntervalSeconds > 0) {
            val lastFinished = syncDao.getCursor(LAST_SYNC_KEY) ?: 0L
            val elapsed = System.currentTimeMillis() - lastFinished
            // A negative value means the clock moved backwards, which must not block syncing
            if (elapsed in 0 until minIntervalSeconds * 1_000) {
                return@withLock SyncSummary(pulled = 0, reconciled = 0, pushed = 0)
            }
        }
x
        val pulled = downloader.pull()
        val reconciled = reconciler.reconcile()
        val pushed = uploader.push()
        syncDao.setCursor(SyncStateEntity(LAST_SYNC_KEY, System.currentTimeMillis()))
        SyncSummary(pulled, reconciled, pushed)
    }

    private companion object {
        const val LAST_SYNC_KEY = "last_sync_at"
    }
}