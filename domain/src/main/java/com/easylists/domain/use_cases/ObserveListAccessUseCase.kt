package com.easylists.domain.use_cases

import com.easylists.domain.models.ListAccess
import com.easylists.domain.repositories.ListAccessRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveListAccessUseCase @Inject constructor(
    private val repo: ListAccessRepository,
) {
    operator fun invoke(): Flow<Map<String, ListAccess>> =
    repo.observeAccess()
}
