package com.easylists.presentation.models

import com.easylists.domain.models.PendingInvite

sealed interface InvitesState {
    data object Loading : InvitesState
    data class Loaded(val invites: List<PendingInvite>) : InvitesState
    data object Failed : InvitesState
}
