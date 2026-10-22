package com.easylists.domain.models

data class Invite(
    val token: String,
    val listId: String,
    val role: InviteRole,
    val expiresAtMillis: Long
)
