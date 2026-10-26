package com.easylists.presentation.models

import com.easylists.domain.models.InviteRole

// State and logic behind the share sheet. One sheet serves both share
// buttons (the list edit sheet and the item screen), so it has its own ViewModel.

enum class ShareError { InvalidEmail, Failed }

data class ShareSheetState(
    val role: InviteRole = InviteRole.Editor,
    val email: String = "",
    val isWorking: Boolean = false,
    // set once the invite exists
    val link: String? = null,
    val error: ShareError? = null,
)
