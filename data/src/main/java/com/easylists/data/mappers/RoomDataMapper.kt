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
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.github.davidepanidev.kotlinextensions.utils.serialization.SerializationManager
import javax.inject.Inject
import kotlin.time.Clock

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
                categoryId = entity.categoryId,
                ownerId = entity.ownerId,
                name = entity.name,
                sortOrder = entity.sortOrder,
                isDirty = entity.isDirty,
                isDeleted = entity.isDeleted,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapEasyListsListToListEntity()
    // maps an easy list to a list entity
    fun mapEasyListsCategoryToCategoryEntity(category: EasyListsCategory): CategoryEntity {
        return if (category.categoryId == null) {
            CategoryEntity(
                name = category.name,
                ownerId = category.ownerId,
                sortOrder = category.sortOrder,
                isDirty = category.isDirty,

            )
        } else {
            CategoryEntity(
                categoryId = category.categoryId!!,
                ownerId = category.ownerId,
                name = category.name,
                sortOrder = category.sortOrder,
                isDirty = category.isDirty,
                isDeleted = category.isDeleted,
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
                listId = entity.listId,
                ownerId = entity.ownerId,
                name = entity.name,
                notes = entity.notes,
                sortOrder = entity.sortOrder,
                isDirty = entity.isDirty,
                isDeleted = entity.isDeleted,
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
            ownerId = list.ownerId,
            notes = list.notes,
            sortOrder = list.sortOrder,
            isDirty = list.isDirty,
            isDeleted = list.isDeleted,
        )
    }
    //endregion


    //region mapEasyListsListToListEntity()
    // maps an easy list to a list entity
    fun mapEasyListsListToListUpdateEntity(list: EasyListsList): ListUpdateEntity {
        return ListUpdateEntity(
            listId = list.listId.toString(),
            name = list.name,
            notes = list.notes,
            sortOrder = list.sortOrder,
            isDirty = list.isDirty,
            isDeleted = list.isDeleted,
            modifiedTimestamp = Clock.System.now().toEpochMilliseconds(),
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
                listItemId = entity.listItemId,
                listId = entity.listId,
                categoryId = entity.categoryId,
                name = entity.name,
                notes = entity.notes,
                quantity = entity.quantity,
                crossedOff = entity.crossedOff == true,
                crossedOffTimestamp = entity.crossedOffTimestamp,
                photoUri = entity.photoUri,
                photoScale = entity.photoScale?: 1.0,
                photoOffsetX = entity.photoOffsetX?: 0.0,
                photoOffsetY = entity.photoOffsetY?: 0.0,
                sortOrder = entity.sortOrder,
                isDirty = entity.isDirty,
                isDeleted = entity.isDeleted,
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
            listId = listItem.listId,
            categoryId = listItem.categoryId,
            quantity = listItem.quantity,
            crossedOff = listItem.crossedOff == true,
            crossedOffTimestamp = listItem.crossedOffTimestamp,
            photoUri = listItem.photoUri,
            photoScale = listItem.photoScale,
            photoOffsetX = listItem.photoOffsetX,
            photoOffsetY = listItem.photoOffsetY,
            isDirty = listItem.isDirty,
            isDeleted = listItem.isDeleted,
        )
    }
    //endregion


    //region mapEasyListsListItemToListItemUpdateEntity()
    // maps an easy list to a list item entity
    fun mapEasyListsListItemToListItemUpdateEntity(listItem: EasyListsListItem): ListItemUpdateEntity {
        return ListItemUpdateEntity(
            listItemId = listItem.listItemId.toString(),
            name = listItem.name,
            notes = listItem.notes,
            sortOrder = listItem.sortOrder,
            categoryUid = listItem.categoryId,
            quantity = listItem.quantity,
            crossedOff = listItem.crossedOff == true,
            crossedOffTimestamp = listItem.crossedOffTimestamp,
            photoUri = listItem.photoUri,
            photoScale = listItem.photoScale,
            photoOffsetX = listItem.photoOffsetX,
            photoOffsetY = listItem.photoOffsetY,
            isDirty = listItem.isDirty,
            isDeleted = listItem.isDeleted,
            modifiedTimestamp = Clock.System.now().toEpochMilliseconds(),
        )
    }
    //endregion


    //
    // EasyListsTag
    //

    //region mapTagEntityListToTagList()
    fun mapTagEntityListToTagList(tagEntityList: List<TagEntity>): List<EasyListsTag> {
        return tagEntityList.map { entity ->
            EasyListsTag(
                tagId = entity.tagId,
                ownerId = entity.ownerId,
                name = entity.name,
                color = entity.color,
                isDirty = entity.isDirty,
                isDeleted = entity.isDeleted,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapTagToTagEntity()
    fun mapTagToTagEntity(easyListsTag: EasyListsTag): TagEntity {
        return TagEntity(
            name = easyListsTag.name,
            ownerId = easyListsTag.ownerId,
            color = easyListsTag.color,
            isDirty = easyListsTag.isDirty
        )
    }
    //endregion


    //region mapTagToTagEntityForUpdate()
    fun mapTagToTagEntityForUpdate(easyListsTag: EasyListsTag): TagEntity {
        return TagEntity(
            tagId = easyListsTag.tagId.toString(),
            ownerId = easyListsTag.ownerId,
            name = easyListsTag.name,
            color = easyListsTag.color,
            isDirty = easyListsTag.isDirty,
            isDeleted = easyListsTag.isDeleted,
            createdTimestamp = easyListsTag.createdTimestamp,
        )
    }
    //endregion


    //
    // EasyListsTag List Item
    //

    //region mapTagListItemEntityListToTagListItemList()
    fun mapTagListItemEntityListToTagListItemList(tagListItemEntityList: List<TagListItemEntity>): List<TagListItem> {
        return tagListItemEntityList.map { entity ->
            TagListItem(
                tagListItemId = entity.tagListItemId,
                tagId = entity.tagId,
                listItemId = entity.listItemId,
                createdTimestamp = entity.createdTimestamp,
                modifiedTimestamp = entity.modifiedTimestamp,
            )
        }
    }
    //endregion


    //region mapTagListItemToTagListItemEntity()
    fun mapTagListItemToTagListItemEntity(tagListItem: TagListItem): TagListItemEntity {
        return TagListItemEntity(
            tagListItemId = tagListItem.tagListItemId.toString(),
            tagId = tagListItem.tagId,
            listItemId = tagListItem.listItemId,
        )
    }
    //endregion


    //region mapTagListItemToTagListItemEntity()
    fun mapTagListItemListToTagListItemEntityList(
        tagListItem: List<TagListItem>
    ): List<TagListItemEntity> {
        return tagListItem.map { tli ->
            TagListItemEntity(
                tagListItemId = tli.tagListItemId.toString(),
                tagId = tli.tagId,
                listItemId = tli.listItemId,
            )
        }
    }
    //endregion

}
