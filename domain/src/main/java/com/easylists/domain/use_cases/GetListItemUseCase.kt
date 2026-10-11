package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.ListItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetListItemUseCase @Inject constructor(
    private val listItemRepository: ListItemRepository,
) {

    //region invoke()
    operator fun invoke(): Flow<List<EasyListsListItem>> {
        return try {
            listItemRepository.getListItemFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }
    //endregion


    //region invoke(listId: String)
    operator fun invoke(listId: String): Flow<List<EasyListsListItem>> {
        return try {
            listItemRepository.getListItemFlow(listId = listId).map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }
    //endregion

}