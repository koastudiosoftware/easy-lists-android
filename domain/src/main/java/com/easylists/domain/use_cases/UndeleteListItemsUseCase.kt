package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class UndeleteListItemsUseCase @Inject constructor(
    private val repository: ListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(listItemIds: List<String>): Result<Int> {
        return repository.undeleteListItems(listItemIds = listItemIds)
    }
    //endregion

}