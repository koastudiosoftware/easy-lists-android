package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class RestoreCrossedOffListItemsUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository
) {

    suspend operator fun invoke(listId: String) {
        listItemRepository.restoreCrossedOffListItems(listId = listId)
    }

}