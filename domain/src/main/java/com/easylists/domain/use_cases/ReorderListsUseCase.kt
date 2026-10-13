package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ListRepository
import javax.inject.Inject

class ReorderListsUseCase @Inject constructor(
    private val repository: ListRepository,
) {

    suspend operator fun invoke(listIds: List<String>) = repository.reorderLists(listIds)

}