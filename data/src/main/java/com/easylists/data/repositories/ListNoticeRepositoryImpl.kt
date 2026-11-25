package com.easylists.data.repositories

import com.easylists.data.db.room.dao.SyncDao
import com.easylists.domain.models.ListNotice
import com.easylists.domain.models.NoticeKind
import com.easylists.domain.repositories.ListNoticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListNoticeRepositoryImpl @Inject constructor(
    private val syncDao: SyncDao,
) : ListNoticeRepository {

    override fun observeNotices(): Flow<List<ListNotice>> {
        return syncDao.observeNotices().map { rows ->
            rows.map { row ->
                ListNotice(
                    id = row.noticeId,
                    kind = if (row.kind == "left") NoticeKind.Left else NoticeKind.Gone,
                    listId = row.listId,
                    listName = row.listName,
                    personName = row.personName,
                )
            }
        }
    }

    override suspend fun clearNotices(noticeIds: List<String>) {
        syncDao.clearNotices(noticeIds)
    }
}
