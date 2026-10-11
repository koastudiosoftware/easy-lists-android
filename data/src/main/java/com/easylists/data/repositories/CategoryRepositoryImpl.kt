package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ListRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val localSource: CategoryLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : CategoryRepository {

    override fun getCategoryFlow(): Flow<List<EasyListsCategory>> {
        return localSource.getCategoryFlow()
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

    override suspend fun addCategory(category: EasyListsCategory): Result<Unit> {
        return Result.runCatching {
            localSource.insert(category = category)
        }
    }

    override suspend fun updateCategory(category: EasyListsCategory): Result<Unit> {
        return Result.runCatching {
            localSource.update(category = category)
        }
    }

    override suspend fun deleteCategories(categoryIds: List<String>): Result<Int> {
        return Result.runCatching {
            localSource.delete(categoryIds = categoryIds)
        }
    }

}

interface CategoryLocalDataSource {

    fun getCategoryFlow(): Flow<List<EasyListsCategory>>
    suspend fun insert(category: EasyListsCategory): Long
    suspend fun update(category: EasyListsCategory)
    suspend fun delete(categoryIds: List<String>): Int

}