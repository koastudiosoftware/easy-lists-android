package com.easylists.domain.repositories

import com.easylists.domain.models.EasyListsList
import kotlinx.coroutines.flow.Flow

interface ListRepository {

    fun getListFlow(): Flow<List<EasyListsList>>

    suspend fun addList(list: EasyListsList): Result<Unit>
    suspend fun updateList(list: EasyListsList): Result<Unit>

}
