package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsTag
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    fun getTagListFlow(): Flow<List<EasyListsTag>>
    fun getTagListFlow(listItemUid: String): Flow<List<EasyListsTag>>

    suspend fun addTag(easyListsTag: EasyListsTag): Result<Unit>
    suspend fun updateTag(easyListsTag: EasyListsTag): Result<Unit>
    suspend fun removeTag(uid: String): Result<Unit>
    suspend fun removeTags(uidList: List<String>): Result<Unit>

}
