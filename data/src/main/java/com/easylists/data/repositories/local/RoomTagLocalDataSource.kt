package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.TagLocalDataSource
import com.easylists.domain.models.EasyListsTag
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomTagLocalDataSource @Inject constructor(
    private val dao: TagDao,
    private val mapper: RoomDataMapper,
    private val dispatchers: DispatcherProvider
) : TagLocalDataSource {

    //region getTagListFlow()
    override fun getTagListFlow(): Flow<List<EasyListsTag>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagEntityListToTagList(it)
            }
    }
    //endregion


    //region getTagListFlow(listItemUid)
    override fun getTagListFlow(listItemUid: String): Flow<List<EasyListsTag>> {
        return dao.get(listItemUid)
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagEntityListToTagList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(easyListsTag: EasyListsTag): Long {
        val mappedTag = mapper.mapTagToTagEntity(easyListsTag)
        return dao.insert(tagEntity = mappedTag)
    }
    //endregion


    //region update()
    override suspend fun update(easyListsTag: EasyListsTag){
        val mappedTag = mapper.mapTagToTagEntityForUpdate(easyListsTag)
        return dao.update(tagEntity = mappedTag)
    }
    //endregion


    //region delete()
    override suspend fun delete(tagId: String) {
        return dao.delete(tagId = tagId)
    }
    //endregion


    //region delete()
    override suspend fun delete(tagIdList: List<String>) {
        return dao.delete(tagIdList = tagIdList)
    }
    //endregion

}
