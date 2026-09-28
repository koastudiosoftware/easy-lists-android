package com.easylists.presentation.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettingsType
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.Themes
import com.easylists.domain.common.VALUE
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.SetAppSettingsUseCase
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
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

    var state by mutableStateOf( SettingsState() )


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

            val capitalization =
                result.find { it[KEY] == AppSettingsKeys.Capitalization.key }?.get(VALUE)

            val enableCamera =
                result.find { it[KEY] == AppSettingsKeys.EnableCamera.key }?.get(VALUE)

            val enablePhotos =
                result.find { it[KEY] == AppSettingsKeys.EnablePhotos.key }?.get(VALUE)

            val enableTags =
                result.find { it[KEY] == AppSettingsKeys.EnableTags.key }?.get(VALUE)

            val groupCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.GroupCrossedOffItems.key }?.get(VALUE)

            val sortCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.SortCrossedOffItems.key }?.get(VALUE)

            val theme = result.find { it[KEY] == AppSettingsKeys.Theme.key }?.get(VALUE)

            state = state.copy(
                capitalization = Capitalization.from(
                    capitalization ?: Capitalization.NoCapitalization.toString()
                ) ?: Capitalization.NoCapitalization,

                enableCamera = enableCamera != "false",

                enablePhotos = enablePhotos != "false",

                enableTags = enableTags != "false",

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


    //region onEnableCameraChanged()
    fun onEnableCameraChanged() {
        val enableCamera = !state.enableCamera
        val enablePhotos = if (enableCamera) true else state.enablePhotos
        state = state.copy(
            enableCamera = enableCamera,
            enablePhotos = enablePhotos
        )
        setBooleanAppSetting(
            key = AppSettingsKeys.EnableCamera.key,
            value = enableCamera
        )
        setBooleanAppSetting(
            key = AppSettingsKeys.EnablePhotos.key,
            value = enablePhotos
        )
    }
    //endregion


    //region onEnableCameraChanged()
    fun onEnablePhotosChanged() {
        val enablePhotos = !state.enablePhotos
        val enableCamera = if (enablePhotos) state.enableCamera else false
        state = state.copy(
            enablePhotos = enablePhotos,
            enableCamera = enableCamera
        )
        setBooleanAppSetting(
            key = AppSettingsKeys.EnablePhotos.key,
            value = enablePhotos
        )
        setBooleanAppSetting(
            key = AppSettingsKeys.EnableCamera.key,
            value = enableCamera
        )
    }
    //endregion


    //region onEnableTagsChanged()
    fun onEnableTagsChanged() {
        state = state.copy(enableTags = !state.enableTags)
        setBooleanAppSetting(
            key = AppSettingsKeys.EnableTags.key,
            value = state.enableTags
        )
    }
    //endregion


    //region setBooleanAppSetting()
    fun setBooleanAppSetting(key: String, value: Boolean) {
        viewModelScope.launch {
            setAppSettingsUseCase(key = key, value = value,
                type = AppSettingsType.Boolean.toString())
        }
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
            is Capitalization -> {
                state = state.copy(capitalization = e)
                setStringAppSetting(key = AppSettingsKeys.Capitalization.key, value = e.value)
            }

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
            is Capitalization -> state.capitalization.toString()
            is GroupCrossedOffItems -> state.groupCrossedOffItems.toString()
            is SortCrossedOffItems -> state.sortCrossedOffItems.toString()
            is Themes -> state.theme.toString()
            else -> ""
        }
    }
    //endregion

}
