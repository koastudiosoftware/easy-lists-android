package com.easylists.data.mappers

import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.github.davidepanidev.kotlinextensions.utils.serialization.SerializationManager
import com.toxicbakery.logging.Arbor
import java.time.Instant
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

class RoomDataMapper @Inject constructor(
    private val serializationManager: SerializationManager,
) {

    //
    // Category
    //

    //region mapCategoryEntityListToEasyListsCategoryList()
    // maps a list of list entities to a list of easy lists
    fun mapCategoryEntityListToEasyListsCategoryList(categoryEntityList: List<CategoryEntity>): List<EasyListsCategory> {
        return categoryEntityList.map { entity ->
            EasyListsCategory(
                uid = entity.uid,
                name = entity.name,
                sortOrder = entity.sortOrder,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapEasyListsListToListEntity()
    // maps an easy list to a list entity
    fun mapEasyListsCategoryToCategoryEntity(category: EasyListsCategory): CategoryEntity {
        return if (category.uid == null) {
            CategoryEntity(
                name = category.name,
                sortOrder = category.sortOrder,
            )
        } else {
            CategoryEntity(
                uid = category.uid!!,
                name = category.name,
                sortOrder = category.sortOrder,
                createdTimestamp = category.createdTimestamp,
            )
        }
    }
    //endregion


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


    //
    // List Item
    //

    //region mapListEntityListToEasyListsListList()
    // maps a list of list entities to a list of easy lists
    fun mapListItemEntityListToEasyListsListItemList(listItemEntityList: List<ListItemEntity>): List<EasyListsListItem> {
        return listItemEntityList.map { entity ->
            EasyListsListItem(
                uid = entity.uid,
                listUid = entity.listUid,
                categoryUid = entity.categoryUid,
                name = entity.name,
                notes = entity.notes,
                quantity = entity.quantity,
                crossedOff = entity.crossedOff == true,
                crossedOffTimestamp = entity.crossedOffTimestamp,
                sortOrder = entity.sortOrder,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapEasyListsListItemToListItemEntity()
    // maps an easy list to a list item entity
    fun mapEasyListsListItemToListItemEntity(listItem: EasyListsListItem): ListItemEntity {
        return ListItemEntity(
            name = listItem.name,
            notes = listItem.notes,
            sortOrder = listItem.sortOrder,
            listUid = listItem.listUid,
            categoryUid = listItem.categoryUid,
            quantity = listItem.quantity,
            crossedOff = listItem.crossedOff == true,
            crossedOffTimestamp = listItem.crossedOffTimestamp,
        )
    }
    //endregion


    //region mapEasyListsListItemToListItemUpdateEntity()
    // maps an easy list to a list item entity
    fun mapEasyListsListItemToListItemUpdateEntity(listItem: EasyListsListItem): ListItemUpdateEntity {
        return ListItemUpdateEntity(
            uid = listItem.uid.toString(),
            name = listItem.name,
            notes = listItem.notes,
            sortOrder = listItem.sortOrder,
            categoryUid = listItem.categoryUid,
            quantity = listItem.quantity,
            crossedOff = listItem.crossedOff == true,
            crossedOffTimestamp = listItem.crossedOffTimestamp,
            modifiedTimestamp = Instant.now().epochSecond,
        )
    }
    //endregion

}
