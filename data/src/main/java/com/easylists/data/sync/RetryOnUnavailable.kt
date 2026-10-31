package com.easylists.data.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Retries a call that failed because the connection wasn't ready yet. The
// first request after the app starts, or after a long idle, can fail as
// "offline" and succeed a moment later.
suspend fun <T> retryOnUnavailable(
    attempts: Int = 3,
    delayMillis: Long = 750,
    block: suspend () -> T,
): T {
    var lastError: FirebaseFirestoreException? = null
    repeat(attempts) {
        try {
            return block()
        } catch (e: FirebaseFirestoreException) {
            val transient = e.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                    e.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
            if (!transient) throw e
            lastError = e
            delay(delayMillis.milliseconds)
        }
    }
    throw lastError ?: IllegalStateException("No attempts were made")
}
