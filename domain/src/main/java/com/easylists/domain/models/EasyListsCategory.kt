package com.easylists.domain.models

import java.time.Instant

data class EasyListsCategory(
    var categoryId: String? = null,
    var name: String,
    var sortOrder: Int? = null,
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,

    var selectedForRemoval: Boolean = false,
)