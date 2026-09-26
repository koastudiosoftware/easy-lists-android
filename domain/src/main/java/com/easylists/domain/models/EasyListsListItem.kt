package com.easylists.domain.models

import java.time.Instant

data class EasyListsListItem(
    var uid: String? = null,
    var listUid: String,
    var category: String? = null,
    var categoryUid: String? = null,
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
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,
)
