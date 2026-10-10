package com.easylists.domain.models

import kotlin.time.Clock

data class EasyListsCategory(
    var categoryId: String? = null,
    var ownerId: String,
    var name: String,
    var sortOrder: Int? = null,
    var isDirty: Boolean = false,
    var isDeleted: Boolean = false,
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    var selectedForRemoval: Boolean = false,
)