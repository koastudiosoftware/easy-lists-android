package com.easylists.domain.models

import java.time.Instant

data class EasyListsListItem(
    var uid: String? = null,
    var listUid: String,
    var categoryUid: String? = null,
    var name: String,
    var quantity: Int? = null,
    var crossedOff: Boolean? = false,
    var notes: String? = null,
    var sortOrder: Int? = null,
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,
)
