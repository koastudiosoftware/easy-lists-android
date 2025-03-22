package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.Tag
import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.TagListItemRepository
import com.easylists.domain.repositories.TagRepository
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

    override suspend fun addTagListItem(tagListItem: TagListItem): Result<Unit> {
        return Result.runCatching {
            localSource.insert(tagListItem = tagListItem)
        }
    }

    override suspend fun removeTag(uid: String): Result<Unit> {
        return Result.runCatching {
            localSource.delete(uid = uid)
        }
    }

}

interface TagListItemLocalDataSource {

    fun getTagListItemListFlow(): Flow<List<TagListItem>>
    fun getTagListItemListFlow(listItemUid: String): Flow<List<TagListItem>>
    suspend fun insert(tagListItem: TagListItem): Long
    suspend fun delete(uid: String)

}