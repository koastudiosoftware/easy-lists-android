package com.easylists.domain.use_cases

import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.ListRepository
import javax.inject.Inject

class RemoveListUseCase @Inject constructor(
    private val listRepository: ListRepository
) {

    //region invoke()
    suspend operator fun invoke(uid: String) {
        listRepository.removeList(uid = uid)
    }
    //endregion

}
