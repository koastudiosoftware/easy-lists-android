package com.easylists.domain.models

import java.time.Instant

data class EasyListsList(
    var uid: String? = null,
    var name: String,
    var notes: String? = null,
    var sortOrder: Int? = null,
    var createdTimestamp: Long = Instant.now().epochSecond,
    var modifiedTimestamp: Long = Instant.now().epochSecond,
)
