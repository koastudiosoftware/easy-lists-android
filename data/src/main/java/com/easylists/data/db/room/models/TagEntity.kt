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
    tableName = "tags",
    indices = [Index(value = ["tag_id"], unique = true)],
)
data class TagEntity(

    @PrimaryKey
    @ColumnInfo(name = "tag_id")
    val tagId: String = Uuid.random().toString(),

    @ColumnInfo(name = "owner_id")
    @SerializedName(value = "owner_id")
    val ownerId: String,

    val name: String,

    val color: String? = null,

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    val isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)
