package com.easylists.data.repositories

import com.easylists.domain.exceptions.EmptyDatabaseException
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.SessionRepository
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CategoryRepositoryImpl @Inject constructor(
    private val localSource: CategoryLocalDataSource,
    private val session: SessionRepository,
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

    override fun getCategoryFlow(
        listId: String,
    ): Flow<List<EasyListsCategory>> {
        return localSource.getCategoryFlow(listId)
            .catch {
                throw if (it is NullPointerException) {
                    EmptyDatabaseException()
                } else it
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.default)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addCategoryForList(
        listId: String,
        name: String,
    ): Result<String> {
        return Result.runCatching {
            val id = Uuid.generateV7().toString()
            localSource.insertForList(
                listId = listId,
                category = EasyListsCategory(
                    categoryId = id,
                    ownerId = session.getUserId(),
                    name = name,
                ),
            )
            id
        }
    }

}

interface CategoryLocalDataSource {

    fun getCategoryFlow(): Flow<List<EasyListsCategory>>
    suspend fun insert(category: EasyListsCategory): Long
    suspend fun update(category: EasyListsCategory)
    suspend fun delete(categoryIds: List<String>): Int
    fun getCategoryFlow(listId: String): Flow<List<EasyListsCategory>>
    suspend fun insertForList(listId: String, category: EasyListsCategory): Long

}