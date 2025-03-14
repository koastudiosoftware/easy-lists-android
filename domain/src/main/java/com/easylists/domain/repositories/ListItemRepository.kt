package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsListItem
import kotlinx.coroutines.flow.Flow

interface ListItemRepository {

    fun getListItemFlow(listUid: String): Flow<List<EasyListsListItem>>

    suspend fun addListItem(listItem: EasyListsListItem): Result<Unit>
    suspend fun removeListItem(uid: String): Result<Unit>

}
