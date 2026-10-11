package com.easylists.presentation.ui.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.use_cases.AddListFlowUseCase
import com.easylists.domain.use_cases.DeleteListsUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.domain.use_cases.UpdateListUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ListsAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListEditorState
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.ListsInteractionState
import com.easylists.presentation.models.validate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ListsViewModel @Inject constructor(
    getListListFlowUseCase: GetListFlowUseCase,
    private val addListUseCase: AddListFlowUseCase,
    private val deleteListsUseCase: DeleteListsUseCase,
    private val updateListUseCase: UpdateListUseCase,
    private val mapper: UiMapper,
    private val session: SessionRepository,
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    // Observed from the data layer, never copied. An error ends the inner flow;
    // bumping refreshTrigger restarts it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val lists: StateFlow<ListListUiState> = refreshTrigger
        .flatMapLatest {
            getListListFlowUseCase()
                .map<List<EasyListsList>?, ListListUiState> { all ->
                    ListListUiState.Success(all.orEmpty().filterNot { it.isDeleted })
                }
                .catch { emit(ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListListUiState.Loading)

    // Add/edit form. null = bottom sheet closed.
    // Compose state (not a flow) so TextFields read and write it synchronously.
    var editor by mutableStateOf<ListEditorState?>(null)
        private set

    var interaction by mutableStateOf(ListsInteractionState())
        private set

    private val currentLists: List<EasyListsList>
        get() = (lists.value as? ListListUiState.Success)?.lists.orEmpty()


    //region Add / edit form
    fun onActionButtonClick(action: ListsAction) {
        interaction = interaction.copy(actionButtonState = action)
        editor = ListEditorState()
    }

    fun onListEditButtonClick(list: EasyListsList) {
        editor = ListEditorState(
            mode = AddEditMode.Edit,
            listId = list.listId.toString(),
            name = list.name,
            notes = list.notes.orEmpty(),
        )
    }

    fun onListNameChange(name: String) { editor = editor?.copy(name = name) }

    fun onListNotesChange(notes: String) { editor = editor?.copy(notes = notes) }

    fun onListBottomSheetDismiss() { editor = null }

    fun addList(onSaved: () -> Unit) {
        val form = editor ?: return
        if (!form.validate(currentLists).canSave) return

        viewModelScope.launch {
            // TODO surface an error if the userId can't be fetched
            val userId = session.getUserId().ifEmpty { return@launch }

            val list = if (form.mode == AddEditMode.Add) {
                EasyListsList(
                    name = form.name.trim(),
                    ownerId = userId,
                    isDirty = true,
                    notes = form.notes.ifEmpty { null },
                )
            } else {
                currentLists.find { it.listId == form.listId }
                    ?.copy(
                        name = form.name.trim(),
                        notes = form.notes.ifEmpty { null },
                        isDirty = true,
                        modifiedTimestamp = Clock.System.now().toEpochMilliseconds(),
                    )
                    ?: return@launch
            }

            if (form.mode == AddEditMode.Add) addListUseCase(list = list) else updateListUseCase(list = list)

            editor = null
            interaction = interaction.copy(selectedListId = null)
            onSaved() // replaces `editor = null`
        }
    }
    //endregion


    //region Selection / delete
    fun showContextIcons(list: EasyListsList?) {
        if (list == null) return
        interaction = interaction.copy(
            selectedListId = if (interaction.selectedListId == null) list.listId.toString() else null,
        )
    }

    fun deleteList() {
        val listId = interaction.selectedListId ?: return

        viewModelScope.launch {
            deleteListsUseCase(listIds = listOf(listId))
            interaction = interaction.copy(selectedListId = null)
        }
    }

    fun setShowConfirmationDialogState(show: Boolean) {
        interaction = interaction.copy(showConfirmationDialog = show)
    }
    //endregion


    //region Refresh
    fun onRefresh() {
        interaction = interaction.copy(isRefreshing = true)
        refreshTrigger.update { it + 1 }
        viewModelScope.launch {
            delay(500.milliseconds) // keeps the indicator from flickering
            interaction = interaction.copy(isRefreshing = false)
        }
    }
    //endregion
}