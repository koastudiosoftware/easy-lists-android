package com.easylists.data.mappers

import com.easylists.data.db.room.models.ListEntity
import com.easylists.domain.models.EasyListsList
import com.github.davidepanidev.kotlinextensions.utils.serialization.SerializationManager
import javax.inject.Inject

class RoomDataMapper @Inject constructor(
    private val serializationManager: SerializationManager,
) {

    //
    // List
    //

    //region mapListEntityListToEasyListsListList()
    // maps a list of list entities to a list of easy lists
    fun mapListEntityListToEasyListsListList(listEntityList: List<ListEntity>): List<EasyListsList> {
        return listEntityList.map { entity ->
            EasyListsList(
                uid = entity.uid,
                name = entity.name,
                notes = entity.notes,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapEasyListsListToListEntity()
    // maps an easy list to a list entity
    fun mapEasyListsListToListEntity(list: EasyListsList): ListEntity {
        return ListEntity(
            name = list.name,
            notes = list.notes,
            sortOrder = list.sortOrder,
        )
    }
    //endregion

}
