package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class DeleteListItemsUseCase @Inject constructor(
    private val repository: ListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(id: String) = invoke(listOf(id))

    suspend operator fun invoke(ids: List<String>) {
        if (ids.isEmpty()) return
        repository.deleteListItems(ids)
    }
    //endregion

}