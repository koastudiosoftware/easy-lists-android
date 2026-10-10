package com.easylists.domain.models

import kotlin.time.Clock

data class EasyListsTag(
    val tagId: String? = null,
    val ownerId: String,
    val name: String,
    val color: String? = null,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
    val createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    var isSelected: Boolean = false,
    var selectedForRemoval: Boolean = false,
)
