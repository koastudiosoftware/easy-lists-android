package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock

@Entity
data class ListUpdateEntity(

    @PrimaryKey
    @ColumnInfo(name = "list_id")
    val listId: String,

    val name: String,

    val notes: String? = null,

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
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    )
