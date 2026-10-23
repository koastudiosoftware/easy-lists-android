package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "lists",
    indices = [Index(value = ["list_id"], unique = true)],
)
data class ListEntity @OptIn(ExperimentalUuidApi::class) constructor(

    @PrimaryKey
    @ColumnInfo(name = "list_id")
    val listId: String = Uuid.generateV7().toString(),

    @ColumnInfo(name = "owner_id")
    @SerializedName(value = "owner_id")
    val ownerId: String,

    val name: String,

    val notes: String? = "",

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    var isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    var isDeleted: Boolean = false,

    // true once the list has been prepared for sharing: its categories belong to the list
    @ColumnInfo(name = "is_shared", defaultValue = "0")
    @SerializedName(value = "is_shared")
    val isShared: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    )
