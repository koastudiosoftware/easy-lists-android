package com.easylists.domain.use_cases

import com.easylists.domain.common.AppSettingsType
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.Themes
import com.easylists.domain.repositories.SettingsRepository
import com.toxicbakery.logging.Arbor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAppSettingsFlowUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(keyMap: Map<String, String>): Flow<Themes> {
        var theme: Flow<Themes>

        withContext(Dispatchers.IO) {
            val keyName = keyMap[KEY].toString()
            theme = settingsRepository.getStringAppSettingTheme(key = keyName)
        }

        return theme
    }

}