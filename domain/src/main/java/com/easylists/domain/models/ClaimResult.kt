package com.easylists.domain.models

sealed interface ClaimResult {
    data class Joined(val listId: String) : ClaimResult
    data object AlreadyMember : ClaimResult
    data object NotFound : ClaimResult   // never existed, already used, or revoked
    data object Expired : ClaimResult
    data object WrongEmail : ClaimResult
    data object NotSignedIn : ClaimResult
}
