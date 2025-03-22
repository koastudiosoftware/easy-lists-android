package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.TagListItemDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.TagListItemLocalDataSource
import com.easylists.domain.models.TagListItem
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomTagListItemLocalDataSource @Inject constructor(
    private val dao: TagListItemDao,
    private val mapper: RoomDataMapper,
    private val dispatchers: DispatcherProvider
) : TagListItemLocalDataSource {

    //region getTagListItemListFlow()
    override fun getTagListItemListFlow(): Flow<List<TagListItem>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagListItemEntityListToTagListItemList(it)
            }
    }
    //endregion


    //region getTagListItemListFlow(listItemUid)
    override fun getTagListItemListFlow(listItemUid: String): Flow<List<TagListItem>> {
        return dao.get(listItemUid)
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagListItemEntityListToTagListItemList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(tagListItem: TagListItem): Long {
        val mappedTagListItem = mapper.mapTagListItemToTagListItemEntity(tagListItem)
        return dao.insert(tagListItemEntity = mappedTagListItem)
    }
    //endregion


    //region delete()
    override suspend fun delete(uid: String) {
        return dao.delete(uid = uid)
    }
    //endregion

}
