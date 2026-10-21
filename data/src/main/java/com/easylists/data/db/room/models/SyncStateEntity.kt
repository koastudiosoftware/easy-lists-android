package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Cursors for the pull: when each query last synced, in microseconds of the server's synced_at.
// Keys are "members", "tags", "tag_links", "list_order", "categories_personal" and, per list,
// "roster:<listId>", "categories:<listId>" and "items:<listId>".
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "cursor_key")
    val cursorKey: String,

    @ColumnInfo(name = "cursor_value")
    val cursorValue: Long,
)
