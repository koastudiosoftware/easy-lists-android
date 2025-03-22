package com.easylists.data.mappers

import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListItemUpdateEntity
import com.easylists.data.db.room.models.ListUpdateEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.Tag
import com.easylists.domain.models.TagListItem
import com.github.davidepanidev.kotlinextensions.utils.serialization.SerializationManager
import java.time.Instant
import javax.inject.Inject

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


    //region mapEasyListsListToListEntity()
    // maps an easy list to a list entity
    fun mapEasyListsListToListUpdateEntity(list: EasyListsList): ListUpdateEntity {
        return ListUpdateEntity(
            uid = list.uid.toString(),
            name = list.name,
            notes = list.notes,
            sortOrder = list.sortOrder,
            modifiedTimestamp = Instant.now().epochSecond,
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


    //
    // Tag
    //

    //region mapTagEntityListToTagList()
    fun mapTagEntityListToTagList(tagEntityList: List<TagEntity>): List<Tag> {
        return tagEntityList.map { entity ->
            Tag(
                uid = entity.uid,
                name = entity.name,
                color = entity.color,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapTagToTagEntity()
    fun mapTagToTagEntity(tag: Tag): TagEntity {
        return TagEntity(
            name = tag.name,
            color = tag.color,
        )
    }
    //endregion


    //
    // Tag List Item
    //

    //region mapTagListItemEntityListToTagListItemList()
    fun mapTagListItemEntityListToTagListItemList(tagListItemEntityList: List<TagListItemEntity>): List<TagListItem> {
        return tagListItemEntityList.map { entity ->
            TagListItem(
                uid = entity.uid,
                listItemUid = entity.listItemUid,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapTagListItemToTagListItemEntity()
    fun mapTagListItemToTagListItemEntity(tagListItem: TagListItem): TagListItemEntity {
        return TagListItemEntity(
            uid = tagListItem.uid,
            listItemUid = tagListItem.listItemUid,
        )
    }
    //endregion

}
