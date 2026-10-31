package com.easylists.presentation.ui.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.InviteRole
import com.easylists.domain.models.ListAccess
import com.easylists.domain.models.ListMember
import com.easylists.domain.models.ListRole
import com.easylists.domain.use_cases.GetPendingInvitesUseCase
import com.easylists.domain.use_cases.LeaveListUseCase
import com.easylists.domain.use_cases.ObserveListAccessUseCase
import com.easylists.domain.use_cases.ObserveMembersUseCase
import com.easylists.domain.use_cases.RemoveMemberUseCase
import com.easylists.domain.use_cases.RevokeInviteUseCase
import com.easylists.domain.use_cases.SetMemberRoleUseCase
import com.easylists.presentation.common.MembersError
import com.easylists.presentation.models.InvitesState
import com.easylists.presentation.models.MembersUiState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// One ViewModel for the members sheet, which opens from several screens
@HiltViewModel
class MembersViewModel @Inject constructor(
    private val observeMembers: ObserveMembersUseCase,
    observeListAccess: ObserveListAccessUseCase,
    private val setMemberRole: SetMemberRoleUseCase,
    private val removeMember: RemoveMemberUseCase,
    private val leaveList: LeaveListUseCase,
    private val getPendingInvites: GetPendingInvitesUseCase,
    private val revokeInvite: RevokeInviteUseCase,
) : ViewModel() {

    private val listId = MutableStateFlow<String?>(null)

    private val _ui = MutableStateFlow(MembersUiState())
    val ui: StateFlow<MembersUiState> = _ui.asStateFlow()

    // Fires once when this person has left the list
    private val _left = Channel<Unit>(Channel.BUFFERED)
    val left = _left.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<ListMember>> = listId
        .filterNotNull()
        .flatMapLatest { observeMembers(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // What this person may do with the list; null until it is known
    val access: StateFlow<ListAccess?> = combine(
        listId.filterNotNull(),
        observeListAccess(),
    ) { id, all -> all[id] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Starts again for a list, so nothing from the last one shows
    fun init(id: String) {
        _ui.value = MembersUiState()
        listId.value = id
    }

    //region Owner: roles and removal
    // Only two roles can be given, so a tap flips between them
    fun onToggleRole(member: ListMember) {
        val id = listId.value ?: return
        val newRole = if (member.role == ListRole.Editor) {
            InviteRole.Viewer
        } else {
            InviteRole.Editor
        }
        viewModelScope.launch { setMemberRole(id, member.userId, newRole) }
    }

    fun onRemoveRequested(member: ListMember) {
        _ui.update { it.copy(confirmRemove = member) }
    }

    fun onRemoveCancelled() {
        _ui.update { it.copy(confirmRemove = null) }
    }

    fun onRemoveConfirmed() {
        val id = listId.value ?: return
        val member = _ui.value.confirmRemove ?: return
        _ui.update { it.copy(confirmRemove = null) }
        viewModelScope.launch { removeMember(id, member.userId) }
    }
    //endregion


    //region Collaborator: leaving
    fun onLeaveRequested() {
        _ui.update { it.copy(confirmLeave = true, error = null) }
    }

    fun onLeaveCancelled() {
        _ui.update { it.copy(confirmLeave = false) }
    }

    fun onLeaveConfirmed() {
        val id = listId.value ?: return
        _ui.update { it.copy(confirmLeave = false, isWorking = true, error = null) }
        viewModelScope.launch {
            try {
                leaveList(id)
                _ui.update { it.copy(isWorking = false) }
                _left.send(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Arbor.e("Leaving failed: ${e.javaClass.simpleName}: ${e.message}", e)
                _ui.update { it.copy(isWorking = false, error = MembersError.LeaveFailed) }
            }
        }
    }
    //endregion


    //region Owner: pending invites
    fun loadInvites() {
        val id = listId.value ?: return
        _ui.update { it.copy(invites = InvitesState.Loading) }
        viewModelScope.launch {
            try {
                val invites = getPendingInvites(id)
                _ui.update { it.copy(invites = InvitesState.Loaded(invites)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Arbor.e("Loading invites failed: ${e.message}", e)
                _ui.update { it.copy(invites = InvitesState.Failed) }
            }
        }
    }

    fun onRevoke(token: String) {
        _ui.update { it.copy(error = null) }
        viewModelScope.launch {
            try {
                revokeInvite(token)
                loadInvites()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Arbor.e("Revoking failed: ${e.javaClass.simpleName}: ${e.message}", e)
                _ui.update { it.copy(error = MembersError.RevokeFailed) }
            }
        }
    }
    //endregion
}