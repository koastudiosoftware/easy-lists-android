package com.easylists.domain.models

// An invitation that hasn't been used or expired yet
data class PendingInvite(
    val token: String,
    val role: InviteRole,
    val expiresAtMillis: Long,
    // set when the invite only works for one email address
    val email: String?,
)
