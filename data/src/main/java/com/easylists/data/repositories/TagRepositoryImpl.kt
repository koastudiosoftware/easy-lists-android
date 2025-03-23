package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.Tag
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.TagRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val localSource: TagLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : TagRepository {

    override fun getTagListFlow(): Flow<List<Tag>> {
        return localSource.getTagListFlow()
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

    override fun getTagListFlow(listItemUid: String): Flow<List<Tag>> {
        return localSource.getTagListFlow(listItemUid)
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

    override suspend fun addTag(tag: Tag): Result<Unit> {
        return Result.runCatching {
            localSource.insert(tag = tag)
        }
    }

    override suspend fun removeTag(uid: String): Result<Unit> {
        return Result.runCatching {
            localSource.delete(uid = uid)
        }
    }

}

interface TagLocalDataSource {

    fun getTagListFlow(): Flow<List<Tag>>
    fun getTagListFlow(listItemUid: String): Flow<List<Tag>>
    suspend fun insert(tag: Tag): Long
    suspend fun delete(uid: String)

}