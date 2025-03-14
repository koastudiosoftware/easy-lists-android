package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.ListRepository
import javax.inject.Inject

class RemoveListItemUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(uid: String) {
        listItemRepository.removeListItem(uid = uid)
    }
    //endregion

}
