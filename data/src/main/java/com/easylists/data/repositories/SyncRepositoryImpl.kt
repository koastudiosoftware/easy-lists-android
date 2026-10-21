package com.easylists.data.repositories

import com.easylists.data.sync.SyncDownloader
import com.easylists.data.sync.SyncReconciler
import com.easylists.data.sync.SyncUploader
import com.easylists.domain.models.SyncSummary
import com.easylists.domain.repositories.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val downloader: SyncDownloader,
    private val reconciler: SyncReconciler,
    private val uploader: SyncUploader,
) : SyncRepository {

    override suspend fun sync(): SyncSummary {
        val pulled = downloader.pull()
        val reconciled = reconciler.reconcile()
        val pushed = uploader.push()
        return SyncSummary(pulled, reconciled, pushed)
    }

}
