package com.easylists.domain.use_cases

import com.easylists.domain.models.ListNotice
import com.easylists.domain.repositories.ListNoticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveListNoticesUseCase @Inject constructor(
    private val repo: ListNoticeRepository,
) {

    operator fun invoke(): Flow<List<ListNotice>> = repo.observeNotices()

}
