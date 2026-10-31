package com.easylists.domain.use_cases

import com.easylists.domain.models.JoinedList
import com.easylists.domain.repositories.InviteRepository
import javax.inject.Inject

class FindJoinedListUseCase @Inject constructor(
    private val repo: InviteRepository,
) {
    suspend operator fun invoke(token: String): JoinedList? =
        repo.findJoinedList(token)
}
