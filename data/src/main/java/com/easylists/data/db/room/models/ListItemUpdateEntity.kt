package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant

@Entity
data class ListItemUpdateEntity(

    @PrimaryKey
    val uid: String,

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

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    var isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    var isDeleted: Boolean = false,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)
