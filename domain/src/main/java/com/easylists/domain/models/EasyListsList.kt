package com.easylists.domain.models

import java.time.Instant

data class EasyListsList(
    var listId: String? = null,
    var name: String,
    var notes: String? = null,
    var sortOrder: Int? = null,
    var isDirty: Boolean = false,
    var isDeleted: Boolean = false,
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,

    var selected: Boolean = false,
)
