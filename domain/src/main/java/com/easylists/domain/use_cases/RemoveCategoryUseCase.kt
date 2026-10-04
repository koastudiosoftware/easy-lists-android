package com.easylists.domain.use_cases

import com.easylists.domain.repositories.CategoryRepository
import javax.inject.Inject

class RemoveCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    //region invoke()
    suspend operator fun invoke(categoryIdList: List<String>) {
        categoryRepository.removeCategories(categoryIdList = categoryIdList)
    }
    //endregion

}