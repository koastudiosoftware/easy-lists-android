package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListNoticeRepository
import javax.inject.Inject

class ClearListNoticesUseCase @Inject constructor(
    private val repo: ListNoticeRepository,
) {

    suspend operator fun invoke(noticeIds: List<String>) =
        repo.clearNotices(noticeIds)

}
