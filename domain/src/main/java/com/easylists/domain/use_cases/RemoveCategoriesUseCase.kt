package com.easylists.domain.use_cases

import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class RemoveCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    //region invoke()
    suspend operator fun invoke(uidList: List<String>) {
        categoryRepository.removeCategories(uidList = uidList)
    }
    //endregion

}