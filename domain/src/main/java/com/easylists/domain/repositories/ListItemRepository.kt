package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsListItem
import kotlinx.coroutines.flow.Flow

interface ListItemRepository {

    fun getListItemFlow(): Flow<List<EasyListsListItem>>
    fun getListItemFlow(listId: String): Flow<List<EasyListsListItem>>

    suspend fun addListItem(listItem: EasyListsListItem): Result<Unit>
    suspend fun deleteListItems(listItemIds: List<String>): Result<Int>
    suspend fun removeCategoryFromListItem(categoryIdList: List<String>): Result<Unit>
    suspend fun updateListItem(listItem: EasyListsListItem): Result<Unit>

}
