package com.easylists.domain.models

import kotlin.time.Clock

data class EasyListsListItem(
    var listItemId: String? = null,
    var listId: String,
    var category: String? = null,
    var categoryId: String? = null,
    var name: String,
    var quantity: Int? = null,
    var crossedOff: Boolean? = false,
    var crossedOffTimestamp: Long? = null,
    var notes: String? = null,
    var photoUri: String? = null,
    var photoOffsetX: Double = 0.0,
    var photoOffsetY: Double = 0.0,
    var photoScale: Double = 0.0,
    var sortOrder: Int? = null,
    var isDirty: Boolean = false,
    var isDeleted: Boolean = false,
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var modifiedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
