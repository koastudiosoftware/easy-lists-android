package com.easylists.domain.models

import kotlin.time.Clock

data class EasyListsList(
    var listId: String? = null,
    var ownerId: String,
    var name: String,
    var notes: String? = null,
    var sortOrder: Int? = null,
    var isDirty: Boolean = false,
    var isDeleted: Boolean = false,
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    var selected: Boolean = false,
)
