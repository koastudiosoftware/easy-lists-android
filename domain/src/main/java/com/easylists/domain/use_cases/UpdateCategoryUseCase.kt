package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ListRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    //region invoke()
    suspend operator fun invoke(category: EasyListsCategory) {
        categoryRepository.updateCategory(category = category)
    }
    //endregion

}