package com.easylists.domain.models

import java.time.Instant

data class Tag(
    val uid: String,
    val name: String,
    val color: String? = null,
    val createdTimestamp: Long = Instant.now().epochSecond,
    val modifiedTimestamp: Long = Instant.now().epochSecond,

    var isSelected: Boolean = false,
)
