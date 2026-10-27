package com.easylists.presentation.models

import com.easylists.domain.models.InvitePreview

enum class JoinFailure { NotFound, Expired, WrongEmail, AlreadyMember, Other }

sealed interface JoinState {
    data object Idle : JoinState

    // A link was opened but nobody is signed in
    data object NeedsSignIn : JoinState

    data object Loading : JoinState
    data class Confirm(val preview: InvitePreview) : JoinState
    data object Joining : JoinState
    data class Failed(val reason: JoinFailure) : JoinState
}

sealed interface JoinEvent {
    data class Joined(val listId: String, val listName: String) : JoinEvent
}