package com.easylists.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.Capitalization
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.common.ViewMode
import com.easylists.domain.models.AuthAccount
import com.easylists.domain.models.LinkResult
import com.easylists.domain.use_cases.LinkAccountUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.ObserveAuthAccountUseCase
import com.easylists.domain.use_cases.SignInWithGoogleUseCase
import com.easylists.domain.use_cases.SignOutUseCase
import com.easylists.domain.use_cases.UpdateAppSettingsUseCase
import com.easylists.presentation.models.AccountUiModel
import com.easylists.presentation.models.SettingsUiState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    private val updateAppSettings: UpdateAppSettingsUseCase,
    observeAuthAccount: ObserveAuthAccountUseCase,
    private val linkAccount: LinkAccountUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signOut: SignOutUseCase,
    @Named("web_client_id") val webClientId: String,
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

    //region Account
    val account: StateFlow<AccountUiModel?> = observeAuthAccount()
        .map { a -> a?.let { AccountUiModel(it.displayName, it.email) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onDebugSignIn(uid: String) {
        viewModelScope.launch {
            val result = linkAccount(
                AuthAccount(uid, "$uid@debug.test", "Debug $uid", null)
            )
            Arbor.i("Debug link result: $result")
        }
    }

    fun onGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true) }
            try {
                val signedIn = signInWithGoogle(idToken)
                when (linkAccount(signedIn)) {
                    LinkResult.Linked, LinkResult.AlreadyLinked -> Arbor.i("Account linked, uid = ${signedIn.uid}")
                    LinkResult.SwitchRequired -> {
                        signOut()   // placeholder until the switch warning exists
                        Arbor.i("Switch required; not handled yet")
                    }
                }
                Arbor.i("Signed in, uid = ${signedIn.uid}")
            } catch (e: CancellationException) {
                Arbor.e("(VM) Sign-in cancelled", e)
                throw e
            } catch (e: Exception) {
                Arbor.e("Sign-in failed", e)
                _uiState.update { it.copy(signInFailed = true) }
            } finally {
                Arbor.i("(VM) Sign-in finished")
                _uiState.update { it.copy(isSigningIn = false) }
            }
        }
    }

    fun onSignInFailed() = _uiState.update { it.copy(signInFailed = true) }
    fun onSignInErrorShown() = _uiState.update { it.copy(signInFailed = false) }
    fun onSignOutClick() = signOut()
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


    //region Firebase crashlytics and analytics
    fun onSendCrashReportsChanged() = update { it.copy(sendCrashReports = !it.sendCrashReports) }

    fun onSendAnalyticsChanged() = update { it.copy(sendAnalytics = !it.sendAnalytics) }
    //endregion

}
