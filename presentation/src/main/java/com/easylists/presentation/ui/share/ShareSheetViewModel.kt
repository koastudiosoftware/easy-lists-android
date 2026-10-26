package com.easylists.presentation.ui.share

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.InviteLinks
import com.easylists.domain.models.InviteRole
import com.easylists.domain.use_cases.CreateInviteUseCase
import com.easylists.domain.use_cases.ObserveAuthAccountUseCase
import com.easylists.presentation.models.ShareError
import com.easylists.presentation.models.ShareSheetState
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

@HiltViewModel
class ShareListViewModel @Inject constructor(
    observeAuthAccount: ObserveAuthAccountUseCase,
    private val createInvite: CreateInviteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ShareSheetState())
    val state: StateFlow<ShareSheetState> = _state.asStateFlow()

    // null until the first value arrives
    val signedIn: StateFlow<Boolean?> = observeAuthAccount()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // A link made for another list, role or email must never show
    fun reset() {
        _state.value = ShareSheetState()
    }

    fun onRoleChange(role: InviteRole) {
        _state.update { it.copy(role = role, link = null, error = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, link = null, error = null) }
    }

    fun createLink(listId: String) {
        val current = _state.value
        if (current.isWorking) return

        val email = current.email.trim()
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(error = ShareError.InvalidEmail) }
            return
        }

        _state.update { it.copy(isWorking = true, error = null, link = null) }
        viewModelScope.launch {
            try {
                val invite = createInvite(
                    listId = listId,
                    role = current.role,
                    email = email.ifEmpty { null },
                )
                _state.update {
                    it.copy(isWorking = false, link = InviteLinks.linkFor(invite.token))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Arbor.e("Create invite failed: ${e.javaClass.simpleName}: ${e.message}", e)
                _state.update { it.copy(isWorking = false, error = ShareError.Failed) }
            }
        }
    }
}
