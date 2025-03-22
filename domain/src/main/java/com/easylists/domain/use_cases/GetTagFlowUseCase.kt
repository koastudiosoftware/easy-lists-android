package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.Tag
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTagFlowUseCase @Inject constructor(
    private val tagRepository: TagRepository,
) {

    operator fun invoke(): Flow<List<Tag>> {
        return try {
            tagRepository.getTagListFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

    operator fun invoke(tagListUid: String): Flow<List<Tag>> {
        return try {
            tagRepository.getTagListFlow(tagListUid).map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

}