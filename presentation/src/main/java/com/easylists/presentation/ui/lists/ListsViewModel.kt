package com.easylists.presentation.ui.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.ListSortMode
import com.easylists.domain.common.ViewMode
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.use_cases.AddListUseCase
import com.easylists.domain.use_cases.DeleteListsUseCase
import com.easylists.domain.use_cases.GetListsUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.ReorderListsUseCase
import com.easylists.domain.use_cases.RestoreListsUseCase
import com.easylists.domain.use_cases.UpdateAppSettingsUseCase
import com.easylists.domain.use_cases.UpdateListUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ListsAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListEditorState
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.ListsEvent
import com.easylists.presentation.models.ListsInteractionState
import com.easylists.presentation.models.validate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ListsViewModel @Inject constructor(
    getListsFlowUseCase: GetListsUseCase,
    observeAppSettings: ObserveAppSettingsUseCase,
    private val addListUseCase: AddListUseCase,
    private val deleteListsUseCase: DeleteListsUseCase,
    private val reorderListsUseCase: ReorderListsUseCase,
    private val restoreListsUseCase: RestoreListsUseCase,
    private val updateListUseCase: UpdateListUseCase,
    private val mapper: UiMapper,
    private val session: SessionRepository,
    private val updateAppSettings: UpdateAppSettingsUseCase,
) : ViewModel() {

    private val _events = Channel<ListsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // null = DataStore hasn't emitted yet
    val viewMode: StateFlow<ViewMode?> = observeAppSettings()
        .map { it.viewMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val refreshTrigger = MutableStateFlow(0)

    // Cold, with no initial value, so `lists` waits for the stored mode instead of
    // flashing the default order first.
    private val storedSortMode: Flow<ListSortMode> = observeAppSettings()
        .map { it.listSortMode }
        .distinctUntilChanged()

    val sortMode: StateFlow<ListSortMode> = storedSortMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListSortMode.NameAsc)

    fun onSortModeChange(mode: ListSortMode) {
        interaction = interaction.copy(isRearranging = mode == ListSortMode.Manual)
        viewModelScope.launch { updateAppSettings { it.copy(listSortMode = mode) } }
    }

    fun onRearrangeDone() {
        interaction = interaction.copy(isRearranging = false)
    }

    // Observed from the data layer, never copied. An error ends the inner flow;
    // bumping refreshTrigger restarts it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val lists: StateFlow<ListListUiState> = refreshTrigger
        .flatMapLatest {
            combine<List<EasyListsList>?, ListSortMode, ListListUiState>(
                getListsFlowUseCase(),
                storedSortMode,
            ) { all, order ->
                ListListUiState.Success(
                    all.orEmpty()
                        .filterNot { it.isDeleted }
                        .sortedWith(order.comparator())
                )
            }
                .catch {
                    emit(
                        ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))
                    )
                }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ListListUiState.Loading
        )

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
            onSaved() // replaces `editor = null`
        }
    }
    //endregion


    //region Delete
    // Deletes the list currently open in the edit bottom sheet, then closes the sheet.
    fun deleteList() {
        val form = editor ?: return
        if (form.mode == AddEditMode.Add) return
        val listId = form.listId

        val deletedName = currentLists.find { it.listId.toString() == listId }?.name
        editor = null

        viewModelScope.launch {
            deleteListsUseCase(listIds = listOf(listId)).onSuccess {
                if (deletedName != null) {
                    _events.send(ListsEvent.ListDeleted(listId, deletedName))
                }
            }
        }
    }
    //endregion


    //region undo list delete
    fun onUndoDelete(listId: String) {
        viewModelScope.launch { restoreListsUseCase(listIds = listOf(listId)) }
    }
    //endregion


    //region Confirmation dialog
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


    //region ListSortMode.comparator()
    private fun ListSortMode.comparator(): Comparator<EasyListsList> = when (this) {
        ListSortMode.NameAsc -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        ListSortMode.NameDesc -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
        ListSortMode.Manual -> Comparator { _, _ -> 0 }
    }
    //endregion


    //region onListsReordered
    fun onListsReordered(listIds: List<String>) {
        viewModelScope.launch { reorderListsUseCase(listIds) }
    }
    //endregion
}