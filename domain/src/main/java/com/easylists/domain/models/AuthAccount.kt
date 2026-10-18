package com.easylists.domain.models

data class AuthAccount(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)