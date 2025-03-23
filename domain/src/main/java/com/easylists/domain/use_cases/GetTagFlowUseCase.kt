package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.repositories.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTagFlowUseCase @Inject constructor(
    private val tagRepository: TagRepository,
) {

    operator fun invoke(): Flow<List<EasyListsTag>> {
        return try {
            tagRepository.getTagListFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

    operator fun invoke(listItemUid: String): Flow<List<EasyListsTag>> {
        return try {
            tagRepository.getTagListFlow(listItemUid = listItemUid).map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

}