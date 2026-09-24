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
    tableName = "list_item",
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["uid"], childColumns = ["category_uid"]),
        ForeignKey(entity = ListEntity::class, parentColumns = ["uid"], childColumns = ["list_uid"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["uid"], unique = true)],
)
data class ListItemEntity @OptIn(ExperimentalUuidApi::class) constructor(

    @PrimaryKey
    val uid: String = Uuid.random().toString(),

    @ColumnInfo(name = "list_uid")
    val listUid: String,

    @ColumnInfo(name = "category_uid")
    val categoryUid: String? = null,

    val name: String,

    val notes: String? = null,

    val quantity: Int? = null,

    @ColumnInfo(name = "crossed_off")
    @SerializedName(value = "crossed_off")
    val crossedOff: Boolean? = null,

    @ColumnInfo(name = "crossed_off_timestamp")
    @SerializedName(value = "crossed_off_timestamp")
    val crossedOffTimestamp: Long? = null,

    @ColumnInfo(name = "photo_uri")
    @SerializedName(value = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "photo_offset_x")
    @SerializedName(value = "photo_offset_x")
    val photoOffsetX: Double? = null,

    @ColumnInfo(name = "photo_offset_y")
    @SerializedName(value = "photo_offset_y")
    val photoOffsetY: Double? = null,

    @ColumnInfo(name = "photo_scale")
    @SerializedName(value = "photo_scale")
    val photoScale: Double? = null,

    @ColumnInfo(name = "sort_order")
    @SerializedName(value = "sort_order")
    val sortOrder: Int? = null,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)