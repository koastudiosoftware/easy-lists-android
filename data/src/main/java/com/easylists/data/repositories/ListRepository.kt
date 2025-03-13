package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.ListRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListRepositoryImpl @Inject constructor(
    private val localSource: ListLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : ListRepository {

    override fun getListFlow(): Flow<List<EasyListsList>> {
        return localSource.getListsFlow()
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

    override suspend fun addList(list: EasyListsList): Result<Unit> {
        return Result.runCatching {
            localSource.insert(list = list)
        }
    }

}

interface ListLocalDataSource {

    fun getListsFlow(): Flow<List<EasyListsList>>
    suspend fun insert(list: EasyListsList): Long

}