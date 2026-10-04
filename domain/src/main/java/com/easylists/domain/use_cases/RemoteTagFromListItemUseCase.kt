package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.TagListItemRepository
import javax.inject.Inject

class RemoveTagFromListItemUseCase @Inject constructor(
    private val tagListItemRepository: TagListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(tagId: String) {
        tagListItemRepository.removeTag(tagId = tagId)
    }
    //endregion


    //region invoke()
    suspend operator fun invoke(tagIdList: List<String>) {
        tagListItemRepository.removeTag(tagIdList = tagIdList)
    }
    //endregion

}
