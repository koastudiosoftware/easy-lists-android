package com.easylists.presentation.models

import com.easylists.domain.models.ListMember
import com.easylists.presentation.common.MembersError

data class MembersUiState(
    // set while the owner is asked to confirm removing someone
    val confirmRemove: ListMember? = null,
    val confirmLeave: Boolean = false,
    val isWorking: Boolean = false,
    val error: MembersError? = null,
    val invites: InvitesState = InvitesState.Loading,
)
