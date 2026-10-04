package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import java.time.Instant

@Entity(
    tableName = "provider_links",
    primaryKeys = ["provider", "provider_user_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["user_id"])],
)
data class ProviderLinkEntity(

    val provider: String,

    @ColumnInfo(name = "provider_user_id")
    val providerUserId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "created_timestamp")
    @SerializedName(value = "created_timestamp")
    var createdTimestamp: Long = Instant.now().epochSecond,

    @ColumnInfo(name = "modified_timestamp")
    @SerializedName(value = "modified_timestamp")
    var modifiedTimestamp: Long = Instant.now().epochSecond,

)
