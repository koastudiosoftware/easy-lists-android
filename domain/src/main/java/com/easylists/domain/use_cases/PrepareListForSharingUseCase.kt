package com.easylists.domain.use_cases

import com.easylists.domain.repositories.ShareRepository
import javax.inject.Inject

class PrepareListForSharingUseCase @Inject constructor(
    private val repo: ShareRepository
) {

    suspend operator fun invoke(listId: String) = repo.prepareListForSharing(listId)

}
