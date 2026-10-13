package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock

@Entity(
    tableName = "list_order",
    primaryKeys = ["user_id", "list_id"],
    indices = [Index(value = ["list_id"])],
)
data class ListOrderEntity(
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "list_id")
    val listId: String,

    @ColumnInfo(name = "sort_index")
    val sortIndex: Int,

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
)