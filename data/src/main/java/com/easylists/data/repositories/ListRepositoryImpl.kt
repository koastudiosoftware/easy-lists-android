package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.SessionRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListRepositoryImpl @Inject constructor(
    private val localSource: ListLocalDataSource,
    private val dispatchers: DispatcherProvider,
    private val session: SessionRepository
) : ListRepository {

    override suspend fun addList(list: EasyListsList): Result<Unit> {
        return Result.runCatching {
            localSource.insert(list = list)
        }
    }

    override suspend fun deleteLists(listIds: List<String>): Result<Int> {
        return Result.runCatching {
            localSource.delete(listIds = listIds)
        }
    }

    override suspend fun updateList(list: EasyListsList): Result<Unit> {
        return Result.runCatching {
            localSource.update(list = list)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getListFlow(): Flow<List<EasyListsList>> {
        return flow { emit(session.getUserId()) }
            .flatMapLatest { userId -> localSource.getListsFlow(userId) }
            .catch { throw if (it is NullPointerException) EmptyDatabaseException() else it }
            .distinctUntilChanged()
            .flowOn(dispatchers.default)
    }

    override suspend fun reorderLists(listIds: List<String>): Result<Unit> {
        return Result.runCatching {
            localSource.reorder(userId = session.getUserId(), listIds = listIds)
        }
    }

    override suspend fun restoreLists(listIds: List<String>): Result<Int> {
        return Result.runCatching {
            localSource.restore(listIds = listIds)
        }
    }
}

interface ListLocalDataSource {

    suspend fun insert(list: EasyListsList): Long
    suspend fun delete(listIds: List<String>): Int
    suspend fun update(list: EasyListsList)

    fun getListsFlow(userId: String): Flow<List<EasyListsList>>   // listDao.get(userId) + your mapper
    suspend fun reorder(userId: String, listIds: List<String>)    // listOrderDao.reorder(...)

    suspend fun restore(listIds: List<String>): Int
}