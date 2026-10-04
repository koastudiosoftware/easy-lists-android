package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "list",
    indices = [Index(value = ["list_id"], unique = true)],
)
data class ListEntity @OptIn(ExperimentalUuidApi::class) constructor(

    @PrimaryKey
    @ColumnInfo(name = "list_id")
    val listId: String = Uuid.random().toString(),

    val name: String,

    val notes: String? = "",

    @ColumnInfo(name = "sort_order")
    @SerializedName(value = "sort_order")
    val sortOrder: Int? = null,

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    var isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    var isDeleted: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)
