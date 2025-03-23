package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.ListItemRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListItemRepositoryImpl @Inject constructor(
    private val localSource: ListItemLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : ListItemRepository {

    override fun getListItemFlow(): Flow<List<EasyListsListItem>> {
        return localSource.getListItemsFlow()
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

    override fun getListItemFlow(listUid: String): Flow<List<EasyListsListItem>> {
        return localSource.getListItemsFlow(listUid = listUid)
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

    override suspend fun addListItem(listItem: EasyListsListItem): Result<Unit> {
        return Result.runCatching {
            localSource.insert(listItem = listItem)
        }
    }

    override suspend fun removeListItem(uid: String): Result<Unit> {
        return Result.runCatching {
            localSource.delete(uid = uid)
        }
    }

    override suspend fun removeCategoryFromListItem(categoryUid: List<String>): Result<Unit> {
        return Result.runCatching {
            localSource.removeCategory(categoryUid = categoryUid)
        }
    }

    override suspend fun updateListItem(listItem: EasyListsListItem): Result<Unit> {
        return Result.runCatching {
            localSource.update(listItem = listItem)
        }
    }

}


interface ListItemLocalDataSource {

    fun getListItemsFlow(): Flow<List<EasyListsListItem>>
    fun getListItemsFlow(listUid: String): Flow<List<EasyListsListItem>>
    suspend fun insert(listItem: EasyListsListItem): Long
    suspend fun removeCategory(categoryUid: List<String>)
    suspend fun delete(uid: String)
    suspend fun update(listItem: EasyListsListItem)

}
