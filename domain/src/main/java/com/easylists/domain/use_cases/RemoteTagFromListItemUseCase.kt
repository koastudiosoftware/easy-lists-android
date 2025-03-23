package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.TagListItemRepository
import javax.inject.Inject

class RemoveTagFromListItemUseCase @Inject constructor(
    private val tagListItemRepository: TagListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(tagUid: String) {
        tagListItemRepository.removeTag(tagUid = tagUid)
    }
    //endregion


    //region invoke()
    suspend operator fun invoke(tagUid: List<String>) {
        tagListItemRepository.removeTag(tagUid = tagUid)
    }
    //endregion

}
