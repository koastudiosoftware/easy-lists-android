package com.easylists.domain.repositories

import com.easylists.domain.models.SyncScheduler
import javax.inject.Inject

class RequestSyncUseCase @Inject constructor(private val scheduler: SyncScheduler) {

    operator fun invoke(delaySeconds: Long = 0, minIntervalSeconds: Long = 0) =
        scheduler.requestSync(delaySeconds, minIntervalSeconds)

}
