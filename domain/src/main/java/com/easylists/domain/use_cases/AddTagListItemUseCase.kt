package com.easylists.domain.use_cases

import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.TagListItemRepository
import javax.inject.Inject

class AddTagListItemUseCase @Inject constructor(
    private val tagListItemRepository: TagListItemRepository
) {

    //region invoke()
    suspend operator fun invoke(tagListItem: TagListItem): Result<Long> {
        return tagListItemRepository.addTagListItem(tagListItem = tagListItem)
    }
    //endregion


    //region invoke()
    suspend operator fun invoke(tagListItem: List<TagListItem>): Result<List<Long>> {
        return tagListItemRepository.addTagListItem(tagListItem = tagListItem)
    }
    //endregion

}