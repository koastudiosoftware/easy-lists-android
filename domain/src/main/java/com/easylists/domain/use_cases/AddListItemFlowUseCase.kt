package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.ListItemRepository
import javax.inject.Inject

class AddListItemFlowUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(listItem: EasyListsListItem) {
        listItemRepository.addListItem(listItem = listItem)
    }
    //endregion

}
