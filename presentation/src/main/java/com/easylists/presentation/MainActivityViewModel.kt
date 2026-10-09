package com.easylists.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettings
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

}
