package com.easylists.domain.repositories

import com.easylists.domain.models.ListNotice
import kotlinx.coroutines.flow.Flow

interface ListNoticeRepository {

    // Notices that haven't been shown yet
    fun observeNotices(): Flow<List<ListNotice>>

    // Only the ones that were shown, so one that arrives while the popup is
    // open isn't cleared without being seen
    suspend fun clearNotices(noticeIds: List<String>)
}
