package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.TagListItemRepository
import javax.inject.Inject

class RemoveTagListItemUseCase @Inject constructor(
    private val tagListItemRepository: TagListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(listItemId: String, tagIdList: List<String>) {
        tagListItemRepository.removeTagListItem(
            listItemId = listItemId,
            tagIdList = tagIdList
        )
    }
    //endregion

}