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

    @ColumnInfo(name = "sort_order")
    @SerializedName(value = "sort_order")
    val sortOrder: Int? = null,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)