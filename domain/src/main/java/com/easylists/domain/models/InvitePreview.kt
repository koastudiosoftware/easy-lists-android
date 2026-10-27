package com.easylists.domain.models

data class InvitePreview(
    val token: String,
    val listName: String,
    val inviterName: String?,
    val role: InviteRole,
    val isExpired: Boolean,
)
