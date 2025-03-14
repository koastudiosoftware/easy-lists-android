package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoryFlowUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {

    operator fun invoke(): Flow<List<EasyListsCategory>> {
        return try {
            categoryRepository.getCategoryFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

}