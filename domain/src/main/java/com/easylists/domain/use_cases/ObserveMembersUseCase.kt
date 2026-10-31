package com.easylists.domain.use_cases

import com.easylists.domain.models.ListMember
import com.easylists.domain.repositories.MembersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMembersUseCase @Inject constructor(
    private val repo: MembersRepository,
) {
    operator fun invoke(listId: String): Flow<List<ListMember>> =
        repo.observeMembers(listId)
}
