package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.ListLocalDataSource
import com.easylists.domain.models.EasyListsList
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.text.insert

class RoomListLocalDataSource @Inject constructor(
    private val dao: ListDao,
    private val mapper: RoomDataMapper,
    private val dispatchers: DispatcherProvider
) : ListLocalDataSource {

    //region getListsFlow()
    override fun getListsFlow(): Flow<List<EasyListsList>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapListEntityListToEasyListsListList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(list: EasyListsList): Long {
        val mappedList = mapper.mapEasyListsListToListEntity(list)
        return dao.insert(listEntity = mappedList)
    }
    //endregion


    //region update()
    override suspend fun update(list: EasyListsList) {
        val mappedList = mapper.mapEasyListsListToListUpdateEntity(list)
        return dao.updatePartial(listUpdateEntity = mappedList)
    }
    //endregion

}
