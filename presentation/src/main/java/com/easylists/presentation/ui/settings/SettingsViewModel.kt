package com.easylists.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.Capitalization
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.common.ViewMode
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.UpdateAppSettingsUseCase
import com.easylists.presentation.models.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    private val updateAppSettings: UpdateAppSettingsUseCase,
) : ViewModel() {

    // Persisted settings. DataStore is the single source of truth.
    // null = DataStore hasn't emitted yet.
    val settings: StateFlow<AppSettings?> = observeAppSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    // Transient screen state
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


    //region UI state
    fun onPullToRefresh(isRefreshing: Boolean) {
        _uiState.update { it.copy(isPullToRefreshing = isRefreshing) }
    }
    //endregion


    //region Toggles
    fun onEnableCameraChanged() = update {
        val camera = !it.enableCamera
        it.copy(
            enableCamera = camera,
            enablePhotos = it.enablePhotos || camera, // camera on forces photos on
        )
    }

    fun onEnablePhotosChanged() = update {
        val photos = !it.enablePhotos
        it.copy(
            enablePhotos = photos,
            enableCamera = it.enableCamera && photos, // photos off forces camera off
        )
    }

    fun onEnableTagsChanged() = update { it.copy(enableTags = !it.enableTags) }

    fun onViewModeToggled() = update {
        it.copy(viewMode = if (it.viewMode == ViewMode.List) ViewMode.Card else ViewMode.List)
    }
    //endregion


    //region List settings
    fun onCapitalizationChanged(value: Capitalization) =
        update { it.copy(capitalization = value) }

    fun onGroupCrossedOffItemsChanged(value: GroupCrossedOffItems) =
        update { it.copy(groupCrossedOffItems = value) }

    fun onSortCrossedOffItemsChanged(value: SortCrossedOffItems) =
        update { it.copy(sortCrossedOffItems = value) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { updateAppSettings(transform) }
    }
    //endregion

}
