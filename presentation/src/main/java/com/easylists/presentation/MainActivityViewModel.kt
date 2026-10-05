package com.easylists.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.common.Themes
import com.easylists.domain.use_cases.GetAppSettingsFlowUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.models.MainActivityState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val getAppSettingsFlowUseCase: GetAppSettingsFlowUseCase,
) : ViewModel() {

    var state by mutableStateOf(MainActivityState())

    private val _themeModeState = MutableStateFlow(Themes.Default)
    val themeModeState: StateFlow<Themes> = _themeModeState.asStateFlow()


    fun init() {
        loadTheme()
    }

    private fun loadTheme() {
        viewModelScope.launch {
            getAppSettingsFlowUseCase(mapOf(
                KEY to AppSettingsKeys.Theme.key,
                TYPE to AppSettingsKeys.Theme.type.toString()

            ))
            .map { Themes.from(it) ?: Themes.Default }
            .collect { _themeModeState.value = it }
        }
    }

}
