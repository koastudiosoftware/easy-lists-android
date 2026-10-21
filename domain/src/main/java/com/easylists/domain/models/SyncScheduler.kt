package com.easylists.domain.models

interface SyncScheduler {

    // delaySeconds waits before starting, so a burst of requests becomes one sync.
    // minIntervalSeconds skips the sync when one finished that recently.
    fun requestSync(delaySeconds: Long = 0, minIntervalSeconds: Long = 0)

}
