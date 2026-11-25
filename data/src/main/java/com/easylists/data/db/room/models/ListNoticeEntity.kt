package com.easylists.data.db.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// One thing the person hasn't been told yet. The ID makes a notice unique:
// one per list for a removed list, one per person and list for someone who left.
@Entity(tableName = "list_notices")
data class ListNoticeEntity(

    @PrimaryKey
    @ColumnInfo(name = "notice_id")
    val noticeId: String,

    // "gone" or "left"
    val kind: String,

    @ColumnInfo(name = "list_id")
    val listId: String,

    @ColumnInfo(name = "list_name")
    val listName: String,

    // For "left": who left, when their name is known
    @ColumnInfo(name = "person_name")
    val personName: String? = null,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: Long,
) {

    companion object {

        fun gone(listId: String, listName: String, now: Long) =
            ListNoticeEntity(
                noticeId = "gone:$listId",
                kind = "gone",
                listId = listId,
                listName = listName,
                personName = null,
                createdTimestamp = now,
            )

        fun left(
            listId: String,
            listName: String,
            userId: String,
            personName: String?,
            now: Long,
        ) = ListNoticeEntity(
            noticeId = "left:$listId:$userId",
            kind = "left",
            listId = listId,
            listName = listName,
            personName = personName,
            createdTimestamp = now,
        )
    }
}
