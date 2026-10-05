package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryFlow(): Flow<List<EasyListsCategory>>

    suspend fun addCategory(category: EasyListsCategory): Result<Unit>
    suspend fun updateCategory(category: EasyListsCategory): Result<Unit>
    suspend fun deleteCategories(categoryIds: List<String>): Result<Int>

}
