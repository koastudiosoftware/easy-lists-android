package com.easylists.domain.models

// One person on a list. isYou is true for the signed-in user.
data class ListMember(
    val userId: String,
    val displayName: String?,
    val role: ListRole,
    val isYou: Boolean,
)
