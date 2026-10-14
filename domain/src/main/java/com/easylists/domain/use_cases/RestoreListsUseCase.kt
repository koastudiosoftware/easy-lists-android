package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListRepository
import javax.inject.Inject

class RestoreListsUseCase @Inject constructor(
    private val repository: ListRepository
) {

    //region invoke()
    suspend operator fun invoke(listIds: List<String>): Result<Int> {
        return repository.restoreLists(listIds = listIds)
    }
    //endregion

}
