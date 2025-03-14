package com.easylists.presentation.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.easylists.presentation.models.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    var state by mutableStateOf( SettingsState() )

    init {
        initAppSettings()
    }


    //region initAppSettings()
    fun initAppSettings() {

    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion

}