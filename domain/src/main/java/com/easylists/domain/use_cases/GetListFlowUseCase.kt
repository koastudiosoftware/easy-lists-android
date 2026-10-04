package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetListFlowUseCase @Inject constructor(
    private val listRepository: ListRepository,
) {

    operator fun invoke(): Flow<List<EasyListsList>> {
        return try {
            listRepository.getListFlow().map { it }
        } catch (e: Exception) {
            flow {
                throw e
            }
        }
    }

}