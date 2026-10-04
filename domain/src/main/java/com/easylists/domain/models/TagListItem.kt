package com.easylists.domain.models

import java.time.Instant

data class TagListItem(
    val tagListItemId: String? = null,
    val tagId: String,
    val listItemId: String,
    val createdTimestamp: Long = Instant.now().epochSecond,
    val modifiedTimestamp: Long = Instant.now().epochSecond,
)
