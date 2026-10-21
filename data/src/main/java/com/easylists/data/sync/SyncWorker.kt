package com.easylists.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.easylists.domain.repositories.SyncRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import com.toxicbakery.logging.Arbor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException

// Runs one sync in the background. WorkManager only starts it when the network is up, and
// retries it with a growing delay when it fails for a reason that might pass.
class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    // WorkManager builds workers itself, so Hilt can't inject into the constructor.
    // This asks Hilt for what the worker needs instead.
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncEntryPoint {
        fun syncRepository(): SyncRepository
    }

    override suspend fun doWork(): Result {
        val repository = EntryPointAccessors
            .fromApplication(applicationContext, SyncEntryPoint::class.java)
            .syncRepository()

        return try {
            val summary = repository.sync(inputData.getLong(KEY_MIN_INTERVAL, 0L))
            Arbor.i(
                "Background sync: pulled ${summary.pulled}, reconciled ${summary.reconciled}, pushed ${summary.pushed}"
            )
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Arbor.e("Background sync failed: ${e.code}", e)
            // When the rules say no, trying again won't change the answer
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) giveUp() else retryOrGiveUp()
        } catch (e: Exception) {
            Arbor.e("Background sync failed: ${e.javaClass.simpleName}: ${e.message}", e)
            retryOrGiveUp()
        }
    }

    private fun giveUp(): Result = Result.failure()

    private fun retryOrGiveUp(): Result =
        if (runAttemptCount >= MAX_ATTEMPTS) Result.failure() else Result.retry()

    companion object {
        const val KEY_MIN_INTERVAL = "min_interval_seconds"
        private const val MAX_ATTEMPTS = 5
    }

}
