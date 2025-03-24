package com.easylists.presentation.ui.masterlists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.Themes
import com.easylists.domain.use_cases.AddListFlowUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.domain.use_cases.RemoveListUseCase
import com.easylists.domain.use_cases.UpdateListUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.MasterListsAction
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.MasterListsState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.haan.resultat.Resultat
import fr.haan.resultat.onFailure
import fr.haan.resultat.onLoading
import fr.haan.resultat.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MasterListsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getListListFlowUseCase: GetListFlowUseCase,
    private val addListUseCase: AddListFlowUseCase,
    private val updateListUseCase: UpdateListUseCase,
    private val removeListUseCase: RemoveListUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var listListFlowJob: Job? = null

    var state by mutableStateOf( MasterListsState() )


    init {
        initAppSettings()
        initListList()
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

            state = state.copy(
                capitalization = Capitalization.from(
                    capitalization ?: Capitalization.NoCapitalization.toString()
                ) ?: Capitalization.NoCapitalization,
            )
        }
    }
    //endregion


    //region addList() :: Add a list to the database
    fun addList() {
        viewModelScope.launch {
            var list = EasyListsList(
                name = state.listName,
                notes = if (state.listNotes.isEmpty()) null else state.listNotes
            )

            if (state.addEditMode == AddEditMode.Add) {
                addListUseCase(list = list)
            } else {
                list.uid = state.listUid
                updateListUseCase(list = list)
            }

            showListBottomSheet()
            state = state.copy(
                listName = "",
                listNotes = "",
                listNameInvalid = false,
                listNameInvalidMessage = "",
                selectedListUid = ""
            )
        }
    }
    //endregion


    //region removeList() :: remove a list from the database
    fun removeList() {
        viewModelScope.launch {
            removeListUseCase(uid = state.selectedListUid)
            state = state.copy(selectedListUid = "")
        }
    }
    //endregion


    //region showListBottomSheet()
    fun showListBottomSheet() {
        state = state.copy(showListBottomSheet = !state.showListBottomSheet)
    }
    //endregion


    //region listIconButtonEnabled()
    fun listIconButtonEnabled(): Boolean {
        if (state.listName.isEmpty()) return false

        if (state.addEditMode == AddEditMode.Edit) return true

        // don't allow duplicate list name
        if (state.listList?.any { it.name.lowercase() == state.listName.lowercase() } == true)
            return false

        return true
    }
    //endregion


    //region listName()
    fun listName(): String {
        return state.listName
    }
    //endregion


    //region listName()
    fun listNotes(): String {
        return state.listNotes
    }
    //endregion


    //region onListNameChange()
    fun onListNameChange(name: String) {
        var listNameInvalidMessage: String
        val isNameInvalid = (state.listList?.any{ it.name == name } == true).let {
            listNameInvalidMessage = if (it) "Name already in use" else ""
            it
        }

        state = state.copy(
            listName = name,
            listNameInvalid = isNameInvalid,
            listNameInvalidMessage = listNameInvalidMessage
        )
    }
    //endregion


    //region onListNotesChange()
    fun onListNotesChange(notes: String) {
        state = state.copy(listNotes = notes)
    }
    //endregion


    //region initListList() :: initialize list of lists from the database
    fun initListList() {
        Arbor.i("initListList()")
        cancelListFlowCollection()

        listListFlowJob = getListListFlowUseCase()
            .onEach {
                handleGetListState(Resultat.success(it))
            }.catch {
                handleGetListState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetListState(result: Resultat<List<EasyListsList>?>) {
        result.onSuccess {
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                listList = it ?: emptyList(),
            )
            Arbor.i("finished")
        }.onFailure {
            state = state.copy(
                uiState = ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))
            )
        }.onLoading {
//            state = state.copy(
//                state = CoinsListUiState.Refreshing(isAutomaticRefresh = true)
//            )
        }
    }


    private fun cancelListFlowCollection() {
        listListFlowJob?.cancel()
        listListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(): () -> Unit = {
        Arbor.i("onPullToRefresh()")
        state = state.copy(
            isPullToRefreshing = true,
            listList = emptyList(),
        )
        viewModelScope.launch {
            initListList()
            delay(500L) // workaround to eliminate sticky pull to refresh indicator
            state = state.copy(isPullToRefreshing = false)
        }
    }
    //endregion


    //region onActionButtonClick()
    fun onActionButtonClick(action: MasterListsAction) {
        state = state.copy(actionButtonState = action)
        showListBottomSheet()
    }
    //endregion


    //region onListEditButtonClick()
    fun onListEditButtonClick(list: EasyListsList) {
        state = state.copy(
            addEditMode = AddEditMode.Edit,
            listUid = list.uid.toString(),
            listName = list.name,
            listNotes = list.notes ?: "",
        )
        showListBottomSheet()
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(list: EasyListsList?) {
        if (list == null) return
        state = state.copy(
            selectedListUid = if (state.selectedListUid.isEmpty()) list.uid.toString() else "",
        )
    }
    //endregion


    //region setShowConfirmationDialogState()
    fun setShowConfirmationDialogState(newState: Boolean) {
        state = state.copy(showConfirmationDialog = newState)
    }
    //endregion


    //region onItemBottomSheetDismiss()
    fun onListBottomSheetDismiss() {
        state = state.copy(
            addEditMode = AddEditMode.Add,
            listUid = "",
            listName = "",
            listNotes = "",
            listNameInvalid = false,
            listNameInvalidMessage = "",
            showListBottomSheet = !state.showListBottomSheet,
        )
    }
    //endregion

}
