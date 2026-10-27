package com.easylists.presentation.ui.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.data.share.PendingInviteStore
import com.easylists.domain.models.ClaimResult
import com.easylists.domain.use_cases.ClaimInviteUseCase
import com.easylists.domain.use_cases.ObserveAuthAccountUseCase
import com.easylists.domain.use_cases.PreviewInviteUseCase
import com.easylists.presentation.models.JoinEvent
import com.easylists.presentation.models.JoinFailure
import com.easylists.presentation.models.JoinState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteJoinViewModel @Inject constructor(
    private val pendingInvites: PendingInviteStore,
    observeAuthAccount: ObserveAuthAccountUseCase,
    private val previewInvite: PreviewInviteUseCase,
    private val claimInvite: ClaimInviteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<JoinState>(JoinState.Idle)
    val state: StateFlow<JoinState> = _state.asStateFlow()

    private val _events = Channel<JoinEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                pendingInvites.token,
                observeAuthAccount().map { it != null }.distinctUntilChanged(),
            ) { token, signedIn -> token to signedIn }
                .collectLatest { (token, signedIn) -> evaluate(token, signedIn) }
        }
    }

    // Runs again whenever the waiting link or the sign-in state changes, so
    // signing in from Settings carries straight on to the confirmation.
    private suspend fun evaluate(token: String?, signedIn: Boolean) {
        when {
            token == null -> _state.value = JoinState.Idle
            !signedIn -> _state.value = JoinState.NeedsSignIn
            else -> {
                _state.value = JoinState.Loading
                try {
                    val preview = previewInvite(token)
                    _state.value = when {
                        preview == null -> JoinState.Failed(JoinFailure.NotFound)
                        preview.isExpired -> JoinState.Failed(JoinFailure.Expired)
                        else -> JoinState.Confirm(preview)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Arbor.e("Invite preview failed: ${e.message}", e)
                    _state.value = JoinState.Failed(JoinFailure.Other)
                }
            }
        }
    }

    fun onJoin() {
        val preview = (_state.value as? JoinState.Confirm)?.preview ?: return
        _state.value = JoinState.Joining
        viewModelScope.launch {
            try {
                when (val result = claimInvite(preview.token)) {
                    is ClaimResult.Joined -> {
                        pendingInvites.clear()
                        _events.send(JoinEvent.Joined(result.listId, preview.listName))
                    }
                    ClaimResult.AlreadyMember -> fail(JoinFailure.AlreadyMember)
                    ClaimResult.NotFound -> fail(JoinFailure.NotFound)
                    ClaimResult.Expired -> fail(JoinFailure.Expired)
                    ClaimResult.WrongEmail -> fail(JoinFailure.WrongEmail)
                    ClaimResult.NotSignedIn -> fail(JoinFailure.Other)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Arbor.e("Joining failed: ${e.javaClass.simpleName}: ${e.message}", e)
                fail(JoinFailure.Other)
            }
        }
    }

    // Cancel, or closing a message: the waiting link is dropped
    fun onDismiss() {
        pendingInvites.clear()
    }

    private fun fail(reason: JoinFailure) {
        _state.value = JoinState.Failed(reason)
    }
}