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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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


// Starts a sync after local edits, at most once per EDIT_WINDOW_MS, and when the app is left.
// Started once from the Application class.
class SyncTriggers @Inject constructor(
    private val syncDao: SyncDao,
    private val scheduler: SyncScheduler,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            // Edits up to this time are already covered by a sync that was requested
            var coveredUntil = 0L

            syncDao.observeNewestDirtyChange()
                // null means nothing is waiting to upload
                .filterNotNull()
                // While waiting below, newer edits replace each other instead of queueing up
                .conflate()
                .collect { editedAt ->
                    if (editedAt <= coveredUntil) return@collect
                    // The first edit opens a window. Edits made while it is open ride along in this sync.
                    delay(EDIT_WINDOW_MS.milliseconds)
                    coveredUntil = System.currentTimeMillis()
                    scheduler.requestSync()
                }
        }
    }

    // Leaving the app must not leave edits waiting for the window to close
    fun flushIfWaiting(scope: CoroutineScope) {
        scope.launch {
            if (syncDao.observeNewestDirtyChange().first() != null) scheduler.requestSync()
        }
    }

    private companion object {
        // 30 seconds between automatic syncs while editing
        const val EDIT_WINDOW_MS = 30_000L
    }

}
