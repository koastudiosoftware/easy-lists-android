package com.easylists.domain.use_cases

import com.easylists.domain.repositories.TagRepository
import javax.inject.Inject

class DeleteTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {

    //region invoke()
    suspend operator fun invoke(tagIds: List<String>): Result<Int> {
        return tagRepository.deleteTags(tagIds = tagIds)
    }
    //endregion

}
