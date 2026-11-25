package com.easylists.domain.use_cases

import com.easylists.domain.repositories.CategoryRepository
import javax.inject.Inject

class RenameCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {

    //region invoke()
    suspend operator fun invoke(categoryId: String, name: String): Result<Unit> {
        return categoryRepository.renameCategory(categoryId, name)
    }
    //endregion

}
