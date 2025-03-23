package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.TagLocalDataSource
import com.easylists.domain.models.Tag
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
    override fun getTagListFlow(): Flow<List<Tag>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagEntityListToTagList(it)
            }
    }
    //endregion


    //region getTagListFlow(listItemUid)
    override fun getTagListFlow(listItemUid: String): Flow<List<Tag>> {
        return dao.get(listItemUid)
            .flowOn(dispatchers.io)
            .map {
                mapper.mapTagEntityListToTagList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(tag: Tag): Long {
        val mappedTag = mapper.mapTagToTagEntity(tag)
        return dao.insert(tagEntity = mappedTag)
    }
    //endregion


    //region delete()
    override suspend fun delete(uid: String) {
        return dao.delete(uid = uid)
    }
    //endregion

}
