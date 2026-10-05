package com.easylists.domain.models

import java.time.Instant

data class EasyListsTag(
    val tagId: String? = null,
    val ownerId: String,
    val name: String,
    val color: String? = null,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
    val createdTimestamp: Long = Instant.now().epochSecond,
    val modifiedTimestamp: Long = Instant.now().epochSecond,

    var isSelected: Boolean = false,
    var selectedForRemoval: Boolean = false,
)
