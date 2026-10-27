package com.easylists.domain.use_cases

import com.easylists.domain.models.InvitePreview
import com.easylists.domain.repositories.InviteRepository
import javax.inject.Inject

class PreviewInviteUseCase @Inject constructor(
    private val repo: InviteRepository,
) {
    suspend operator fun invoke(token: String): InvitePreview? =
        repo.previewInvite(token)
}
