package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "tag_list_item",
    foreignKeys = [
        ForeignKey(entity = ListItemEntity::class, parentColumns = ["uid"], childColumns = ["list_item_uid"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["uid"], unique = true)],
)
data class TagListItemEntity @OptIn(ExperimentalUuidApi::class) constructor(

    @PrimaryKey
    val uid: String = Uuid.random().toString(),

    @ColumnInfo(name = "list_item_uid")
    @SerializedName(value = "list_item_uid")
    var listItemUid: String,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)
