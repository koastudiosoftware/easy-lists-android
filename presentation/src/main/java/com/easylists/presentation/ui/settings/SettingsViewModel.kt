package com.easylists.presentation.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.Themes
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.SetAppSettingsUseCase
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.models.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val setAppSettingsUseCase: SetAppSettingsUseCase,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    var state by mutableStateOf(SettingsState())


    init {
        initAppSettings()
    }


    //region initAppSettings()
    fun initAppSettings() {
        viewModelScope.launch {
            val result = getAppSettingsUseCase(
                keys = AppSettingsKeys.entries.map {
                    mapOf(
                        KEY to it.key,
                        TYPE to it.type.toString()
                    )
                },
            )

            val groupCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.GroupCrossedOffItems.key }?.get(VALUE)

            val sortCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.SortCrossedOffItems.key }?.get(VALUE)

            val theme = result.find { it[KEY] == AppSettingsKeys.Theme.key }?.get(VALUE)

            state = state.copy(
                groupCrossedOffItems = GroupCrossedOffItems.from(
                    groupCrossedOffItems ?: GroupCrossedOffItems.AllTogether.toString()
                ) ?: GroupCrossedOffItems.AllTogether,

                sortCrossedOffItems = SortCrossedOffItems.from(
                    sortCrossedOffItems ?: SortCrossedOffItems.MostRecentOnTop.toString()
                ) ?: SortCrossedOffItems.MostRecentOnTop,

                theme = Themes.from(theme ?: Themes.Solarized.toString()) ?: Themes.Solarized,
            )
        }
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region setStringAppSetting()
    fun setStringAppSetting(key: String, value: String) {
        viewModelScope.launch {
            setAppSettingsUseCase(key = key, value = value)
        }
    }
    //endregion


    //region onListSettingsChanged()
    fun <E : Enum<E>> onListSettingsChanged(e: E) {
        when (e) {
            is GroupCrossedOffItems -> {
                state = state.copy(groupCrossedOffItems = e)
                setStringAppSetting(key = AppSettingsKeys.GroupCrossedOffItems.key, value = e.value)
            }

            is SortCrossedOffItems -> {
                state = state.copy(sortCrossedOffItems = e)
                setStringAppSetting(key = AppSettingsKeys.SortCrossedOffItems.key, value = e.value)
            }

            is Themes -> {
                state = state.copy(theme = e)
                setStringAppSetting(key = AppSettingsKeys.Theme.key, value = e.value)
            }
        }
    }
    //endregion


    //region listSettingsSelected()
    fun <E : Enum<E>> listSettingsSelected(e: E): String {
        return when (e) {
            is GroupCrossedOffItems -> state.groupCrossedOffItems.toString()
            is SortCrossedOffItems -> state.sortCrossedOffItems.toString()
            is Themes -> state.theme.toString()
            else -> ""
        }
    }
    //endregion

}
