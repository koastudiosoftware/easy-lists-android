package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsTag
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

    override fun getTagListFlow(): Flow<List<EasyListsTag>> {
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

    override fun getTagListFlow(listItemUid: String): Flow<List<EasyListsTag>> {
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

    override suspend fun addTag(easyListsTag: EasyListsTag): Result<Unit> {
        return Result.runCatching {
            localSource.insert(easyListsTag = easyListsTag)
        }
    }

    override suspend fun updateTag(easyListsTag: EasyListsTag): Result<Unit> {
        return Result.runCatching {
            localSource.update(easyListsTag = easyListsTag)
        }
    }

    override suspend fun deleteTags(tagIds: List<String>): Result<Int> {
        return Result.runCatching {
            localSource.delete(tagIds = tagIds)
        }
    }

}

interface TagLocalDataSource {

    fun getTagListFlow(): Flow<List<EasyListsTag>>
    fun getTagListFlow(listItemUid: String): Flow<List<EasyListsTag>>
    suspend fun insert(easyListsTag: EasyListsTag): Long
    suspend fun update(easyListsTag: EasyListsTag)
    suspend fun delete(tagIds: List<String>): Int

}