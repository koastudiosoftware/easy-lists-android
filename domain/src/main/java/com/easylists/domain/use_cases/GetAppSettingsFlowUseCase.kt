package com.easylists.domain.use_cases

import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsFlowUseCase @Inject constructor(
    private val repository: SettingsRepository   // interface, defined in domain
) {

//    operator fun invoke(keyMap: Map<String, String>): Flow<String> =
//        repository.getAppSettingFlow(
//            key = keyMap[KEY].orEmpty(),
//            type = keyMap[TYPE].orEmpty(),
//        )

}
