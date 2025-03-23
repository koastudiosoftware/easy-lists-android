package com.easylists.domain.repositories

import com.easylists.domain.models.TagListItem
import kotlinx.coroutines.flow.Flow

interface TagListItemRepository {

    fun getTagListItemListFlow(): Flow<List<TagListItem>>
    fun getTagListItemListFlow(listItemUid: String): Flow<List<TagListItem>>

    suspend fun addTagListItem(tagListItem: TagListItem): Result<Long>
    suspend fun addTagListItem(tagListItem: List<TagListItem>): Result<List<Long>>
    suspend fun removeTagListItem(listItemUid: String, tagUidList: List<String>): Result<Unit>

}
