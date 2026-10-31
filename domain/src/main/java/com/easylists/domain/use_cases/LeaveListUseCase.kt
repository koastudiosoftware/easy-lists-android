package com.easylists.domain.use_cases

import com.easylists.domain.repositories.MembersRepository
import javax.inject.Inject

class LeaveListUseCase @Inject constructor(
    private val repo: MembersRepository,
) {
    suspend operator fun invoke(listId: String) = repo.leaveList(listId)
}
