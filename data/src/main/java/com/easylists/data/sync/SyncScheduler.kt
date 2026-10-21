package com.easylists.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.easylists.data.db.room.dao.SyncDao
import com.easylists.domain.models.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Schedules a background sync. Asking again replaces a sync that is still waiting, so a burst of
// requests (a burst of edits, say) ends up as one sync, run after the last request's delay.
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncScheduler {

    override fun requestSync(delaySeconds: Long, minIntervalSeconds: Long) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(workDataOf(SyncWorker.KEY_MIN_INTERVAL to minIntervalSeconds))
            // Only runs when there is a network; WorkManager waits for one instead of failing
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()

        // One slot per interval, so a throttled foreground request never replaces a waiting edit sync
        WorkManager.getInstance(context)
            .enqueueUniqueWork("$WORK_NAME_PREFIX$minIntervalSeconds", ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val WORK_NAME_PREFIX = "sync_"
        const val BACKOFF_SECONDS = 30L
    }
}


// Starts a sync a few seconds after every local edit.
// Started once from the Application class.
class SyncTriggers @Inject constructor(
    private val syncDao: SyncDao,
    private val scheduler: SyncScheduler,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            syncDao.observeNewestDirtyChange()
                // null means nothing is waiting to upload
                .filterNotNull()
                .distinctUntilChanged()
                .collect { scheduler.requestSync(delaySeconds = EDIT_DELAY_SECONDS) }
        }
    }

    private companion object {
        // Long enough to cover a burst of typing, short enough that a change goes up promptly
        const val EDIT_DELAY_SECONDS = 5L
    }
}