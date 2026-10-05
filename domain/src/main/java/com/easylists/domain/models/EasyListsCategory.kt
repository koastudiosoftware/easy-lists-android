package com.easylists.domain.models

import java.time.Instant

data class EasyListsCategory(
    var categoryId: String? = null,
    var ownerId: String,
    var name: String,
    var sortOrder: Int? = null,
    var isDirty: Boolean = false,
    var isDeleted: Boolean = false,
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,

    var selectedForRemoval: Boolean = false,
)