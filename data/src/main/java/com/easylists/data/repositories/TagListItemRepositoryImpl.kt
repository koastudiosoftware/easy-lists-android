package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.TagListItemRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagListItemRepositoryImpl @Inject constructor(
    private val localSource: TagListItemLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : TagListItemRepository {

    override fun getTagListItemListFlow(): Flow<List<TagListItem>> {
        return localSource.getTagListItemListFlow()
            .catch {
                throw if (it is NullPointerException) {
                    EmptyDatabaseException()
                } else it
            }
            .distinctUntilChanged()
            .map {
                it
            }.flowOn(dispatchers.default)
    }

    override fun getTagListItemListFlow(listItemUid: String): Flow<List<TagListItem>> {
        return localSource.getTagListItemListFlow(listItemUid)
            .catch {
                throw if (it is NullPointerException) {
                    EmptyDatabaseException()
                } else it
            }
            .distinctUntilChanged()
            .map {
                it
            }.flowOn(dispatchers.default)
    }

    override suspend fun addTagListItem(tagListItem: TagListItem): Result<Long> {
        return Result.runCatching {
            localSource.insert(tagListItem = tagListItem)
        }
    }

    override suspend fun addTagListItem(tagListItem: List<TagListItem>): Result<List<Long>> {
        return Result.runCatching {
            localSource.insert(tagListItem = tagListItem)
        }
    }

    override suspend fun removeTag(tagId: String): Result<Unit> {
        return Result.runCatching {
            localSource.delete(tagId = tagId)
        }
    }

    override suspend fun removeTag(tagIdList: List<String>): Result<Unit> {
        return Result.runCatching {
            localSource.delete(tagIdList = tagIdList)
        }
    }

    override suspend fun removeTagListItem(
        listItemId: String,
        tagIdList: List<String>
    ): Result<Unit> {
        return Result.runCatching {
            localSource.delete(listItemId = listItemId, tagIdList = tagIdList)
        }
    }

}

interface TagListItemLocalDataSource {

    fun getTagListItemListFlow(): Flow<List<TagListItem>>
    fun getTagListItemListFlow(listItemUid: String): Flow<List<TagListItem>>
    suspend fun insert(tagListItem: TagListItem): Long
    suspend fun insert(tagListItem: List<TagListItem>): List<Long>
    suspend fun delete(tagId: String)
    suspend fun delete(tagIdList: List<String>)
    suspend fun delete(listItemId: String, tagIdList: List<String>)

}