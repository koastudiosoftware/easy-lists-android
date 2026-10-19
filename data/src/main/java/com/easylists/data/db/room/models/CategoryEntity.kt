package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["list_id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
    indices = [
        Index(value = ["category_id"], unique = true),
        Index(value = ["list_id"]),
    ],
)
data class CategoryEntity @OptIn(ExperimentalUuidApi::class) constructor(

    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val categoryId: String = Uuid.generateV7().toString(),

    @ColumnInfo(name = "owner_id")
    @SerializedName(value = "owner_id")
    val ownerId: String,

    val name: String,

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    var isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    var isDeleted: Boolean = false,

    // null = a personal category; set = the category belongs to that (shared) list
    @ColumnInfo(name = "list_id")
    @SerializedName(value = "list_id")
    val listId: String? = null,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    val createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    val modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

)
