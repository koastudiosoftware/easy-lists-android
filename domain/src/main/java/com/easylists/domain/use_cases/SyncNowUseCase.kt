package com.easylists.domain.use_cases

import com.easylists.domain.models.SyncSummary
import com.easylists.domain.repositories.SyncRepository
import javax.inject.Inject

class SyncNowUseCase @Inject constructor(
    private val repo: SyncRepository
) {

    suspend operator fun invoke(): SyncSummary = repo.sync()

}
