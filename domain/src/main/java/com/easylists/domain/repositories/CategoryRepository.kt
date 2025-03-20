package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryFlow(): Flow<List<EasyListsCategory>>

    suspend fun addCategory(category: EasyListsCategory): Result<Unit>
    suspend fun removeCategories(uidList: List<String>): Result<Unit>

}
