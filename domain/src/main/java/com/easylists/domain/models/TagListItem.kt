package com.easylists.domain.models

import java.time.Instant

data class TagListItem(
    val uid: String? = null,
    val tagUid: String,
    val listItemUid: String,
    val createdTimestamp: Long = Instant.now().epochSecond,
    val modifiedTimestamp: Long = Instant.now().epochSecond,
)
