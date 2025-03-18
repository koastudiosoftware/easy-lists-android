package com.easylists.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.Themes
import com.easylists.domain.use_cases.GetAppSettingsFlowUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.models.MainActivityState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val getAppSettingsFlowUseCase: GetAppSettingsFlowUseCase,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    var state by mutableStateOf(MainActivityState())

    lateinit var themeModeState: StateFlow<Any>

    fun init() {
        initAppSettings()
    }


    fun initAppSettings() {
        viewModelScope.launch {
            themeModeState = getAppSettingsFlowUseCase(
                keyMap = mapOf(
                    KEY to AppSettingsKeys.Theme.key.toString(),
                    TYPE to AppSettingsKeys.Theme.type.toString()
                ),
            ).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = Themes.Default
            )
        }
    }

}
