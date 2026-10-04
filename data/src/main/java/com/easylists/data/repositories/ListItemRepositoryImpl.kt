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

    //region getListItemFlow()
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
    //endregion

    //region getListItemFlow()
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
    //endregion

    //region addListItem()
    override suspend fun addListItem(listItem: EasyListsListItem): Result<Unit> {
        return Result.runCatching {
            localSource.insert(listItem = listItem)
        }
    }
    //endregion

    //region deleteListItems()
    override suspend fun deleteListItems(listItemUids: List<String>): Result<Int> {
        return Result.runCatching {
            localSource.delete(listItemUids = listItemUids)
        }
    }
    //endregion

    //region removeCategoryFromListItem()
    override suspend fun removeCategoryFromListItem(categoryUid: List<String>): Result<Unit> {
        return Result.runCatching {
            localSource.removeCategory(categoryUid = categoryUid)
        }
    }
    //endregion

    //region updateListItem()
    override suspend fun updateListItem(listItem: EasyListsListItem): Result<Unit> {
        return Result.runCatching {
            localSource.update(listItem = listItem)
        }
    }
    //endregion

}


interface ListItemLocalDataSource {

    fun getListItemsFlow(): Flow<List<EasyListsListItem>>
    fun getListItemsFlow(listUid: String): Flow<List<EasyListsListItem>>
    suspend fun delete(listItemUids: List<String>): Int
    suspend fun insert(listItem: EasyListsListItem): Long
    suspend fun removeCategory(categoryUid: List<String>)
    suspend fun update(listItem: EasyListsListItem)

}
