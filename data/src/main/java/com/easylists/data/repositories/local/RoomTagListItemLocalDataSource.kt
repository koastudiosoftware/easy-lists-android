package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.TagListItemDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.TagListItemLocalDataSource
import com.easylists.domain.models.TagListItem
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import com.toxicbakery.logging.Arbor
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


    //region insert()
    override suspend fun insert(tagListItem: List<TagListItem>): List<Long> {
        val mappedTagListItem = mapper.mapTagListItemListToTagListItemEntityList(tagListItem)
        return dao.insert(tagListItemEntity = mappedTagListItem)
    }
    //endregion


    //region delete()
    override suspend fun delete(tagUid: String) {
        return dao.delete(tagUid = tagUid)
    }


    override suspend fun delete(tagUid: List<String>) {
        return dao.delete(tagUid = tagUid)
    }
    //endregion


    //region delete()
    override suspend fun delete(listItemUid: String, tagUidList: List<String>) {
        return dao.delete(listItemUid = listItemUid, tagUidList = tagUidList)
    }
    //endregion

}
