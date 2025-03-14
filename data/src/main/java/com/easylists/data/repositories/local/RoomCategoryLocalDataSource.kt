package com.easylists.data.repositories.local

import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.mappers.RoomDataMapper
import com.easylists.data.repositories.CategoryLocalDataSource
import com.easylists.domain.models.EasyListsCategory
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomCategoryLocalDataSource @Inject constructor(
    private val dao: CategoryDao,
    private val mapper: RoomDataMapper,
    private val dispatchers: DispatcherProvider
) : CategoryLocalDataSource {

    //region getCategoryFlow()
    override fun getCategoryFlow(): Flow<List<EasyListsCategory>> {
        return dao.get()
            .flowOn(dispatchers.io)
            .map {
                mapper.mapCategoryEntityListToEasyListsCategoryList(it)
            }
    }
    //endregion


    //region insert()
    override suspend fun insert(category: EasyListsCategory): Long {
        val mappedList = mapper.mapEasyListsCategoryToCategoryEntity(category)
        return dao.insert(categoryEntity = mappedList)
    }
    //endregion


    //region delete()
    override suspend fun delete(uid: String) {
        return dao.delete(uid = uid)
    }
    //endregion

}
