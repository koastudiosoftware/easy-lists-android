package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlin.time.Clock

@Entity(
    tableName = "list_members",
    primaryKeys = ["list_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["list_id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
    indices = [Index(value = ["user_id"])],
)
data class ListMemberEntity(

    @ColumnInfo(name = "list_id")
    @SerializedName(value = "list_id")
    val listId: String,

    @ColumnInfo(name = "user_id")
    @SerializedName(value = "user_id")
    val userId: String,

    // "owner", "editor" or "viewer"
    val role: String,

    // Copied from the OAuth provider when the member joins; allowed to go stale
    @ColumnInfo(name = "display_name")
    @SerializedName(value = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "is_dirty")
    @SerializedName(value = "is_dirty")
    var isDirty: Boolean = true,

    // Soft-deleted so that leaving a list or being removed can sync
    @ColumnInfo(name = "is_deleted")
    @SerializedName(value = "is_deleted")
    var isDeleted: Boolean = false,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

)