package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class RemoveCategoryFromListItemUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(categoryIdList: List<String>): Result<Unit> {
        return listItemRepository.removeCategoryFromListItem(categoryIdList = categoryIdList)
    }
    //endregion

}
