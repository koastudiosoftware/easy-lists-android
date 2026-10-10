package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.repositories.TagRepository
import javax.inject.Inject

class AddTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {

    //region invoke()
    suspend operator fun invoke(easyListsTag: EasyListsTag): Result<Unit> {
        return tagRepository.addTag(easyListsTag = easyListsTag)
    }
    //endregion

}
