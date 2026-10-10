package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Entity(
    tableName = "tag_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ListItemEntity::class,
            parentColumns = ["list_item_id"],
            childColumns = ["list_item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["tag_list_item_id"], unique = true),
        Index(value = ["tag_id"]),
        Index(value = ["list_item_id"]),
    ],
)
data class TagListItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "tag_list_item_id")
    val tagListItemId: String = Uuid.random().toString(),

    @ColumnInfo(name = "tag_id")
    @SerializedName(value = "tag_id")
    var tagId: String,

    @ColumnInfo(name = "list_item_id")
    @SerializedName(value = "list_item_id")
    var listItemId: String,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    )
