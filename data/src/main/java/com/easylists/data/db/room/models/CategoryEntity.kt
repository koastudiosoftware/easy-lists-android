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
    tableName = "category",
    indices = [Index(value = ["category_id"], unique = true)],
)
data class CategoryEntity(

    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val categoryId: String = Uuid.random().toString(),

    val name: String,

    @ColumnInfo(name = "sort_order")
    @SerializedName(value = "sort_order")
    val sortOrder: Int? = null,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    val createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    val modifiedTimestamp: Long = Instant.now().epochSecond,

)
