package com.easylists.domain.use_cases

import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.TagRepository
import javax.inject.Inject

class RemoveTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {

    //region invoke()
    suspend operator fun invoke(tagIdList: List<String>) {
        tagRepository.removeTags(tagIdList = tagIdList)
    }
    //endregion

}