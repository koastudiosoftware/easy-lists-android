package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)],
)
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = Uuid.random().toString(),

    val email: String? = null,

    val displayName: String? = null,

    val profilePictureUrl: String? = null,

    val syncEnabled: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    )
