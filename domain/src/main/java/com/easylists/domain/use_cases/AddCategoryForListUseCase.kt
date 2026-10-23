package com.easylists.domain.use_cases

import com.easylists.domain.repositories.CategoryRepository
import javax.inject.Inject

class AddCategoryForListUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {

    //region invoke()
    suspend operator fun invoke(listId: String, name: String): Result<String> {
        return categoryRepository.addCategoryForList(listId, name)
    }
    //endregion

}
