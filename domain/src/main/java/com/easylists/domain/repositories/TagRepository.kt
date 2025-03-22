package com.easylists.domain.repositories

import com.easylists.domain.models.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    fun getTagListFlow(): Flow<List<Tag>>
    fun getTagListFlow(listItemUid: String): Flow<List<Tag>>

    suspend fun addTag(list: Tag): Result<Unit>
    suspend fun removeTag(uid: String): Result<Unit>

}
