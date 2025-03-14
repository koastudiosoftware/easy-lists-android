package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetListItemFlowUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository,
) {

    operator fun invoke(listUid: String): Flow<List<EasyListsListItem>> {
        return try {
            listItemRepository.getListItemFlow(listUid = listUid).map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

}