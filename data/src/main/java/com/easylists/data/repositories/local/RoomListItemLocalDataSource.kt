package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.ListItemLocalDataSource
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomListItemLocalDataSource @Inject constructor(
    private val dao: ListItemDao,
    private val mapper: RoomDataMapper,
    private val dispatchers: DispatcherProvider
) : ListItemLocalDataSource {

    //region getListsFlow()
    override fun getListItemsFlow(): Flow<List<EasyListsListItem>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapListItemEntityListToEasyListsListItemList(it)
            }
    }
    //endregion


    //region getListsFlow()
    override fun getListItemsFlow(listUid: String): Flow<List<EasyListsListItem>> {
        return dao.get(listUid = listUid)
            .flowOn(dispatchers.io)
            .map {
                mapper.mapListItemEntityListToEasyListsListItemList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(listItem: EasyListsListItem): Long {
        val mappedList = mapper.mapEasyListsListItemToListItemEntity(listItem)
        return dao.insert(listItemEntity = mappedList)
    }

    //region removeCategory()
    override suspend fun removeCategory(categoryUid: List<String>) {
        return dao.removeCategory(categoryUid = categoryUid)
    }
    //endregion

    //region update()
    override suspend fun update(listItem: EasyListsListItem) {
        val mappedList = mapper.mapEasyListsListItemToListItemUpdateEntity(listItem)
        return dao.updatePartial(listItemUpdateEntity = mappedList)
    }
    //endregion

}
