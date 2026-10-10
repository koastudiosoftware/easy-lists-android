package com.easylists.domain.models

import kotlin.time.Clock

data class TagListItem(
    val tagListItemId: String? = null,
    val tagId: String,
    val listItemId: String,
    val createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
