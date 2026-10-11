package com.easylists.domain.use_cases

import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.TagListItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTagListItemUseCase @Inject constructor(
    private val tagListItemRepository: TagListItemRepository,
) {

    //region invoke()
    operator fun invoke(): Flow<List<TagListItem>> {
        return try {
            tagListItemRepository.getTagListItemListFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }
    //endregion


    //region invoke(listItemUid)
    operator fun invoke(listItemUid: String): Flow<List<TagListItem>> {
        return try {
            tagListItemRepository.getTagListItemListFlow(listItemUid = listItemUid).map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }
    //endregion

}