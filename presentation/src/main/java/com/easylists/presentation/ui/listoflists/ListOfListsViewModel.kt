package com.easylists.presentation.ui.listoflists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.use_cases.AddListFlowUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.presentation.common.ListOfListsAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListListUiState
import com.easylists.presentation.models.ListOfListsState
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.haan.resultat.Resultat
import fr.haan.resultat.onFailure
import fr.haan.resultat.onLoading
import fr.haan.resultat.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListOfListsViewModel @Inject constructor(
    private val getListListFlowUseCase: GetListFlowUseCase,
    private val addListUseCase: AddListFlowUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var listListFlowJob: Job? = null

    var state by mutableStateOf( ListOfListsState() )


    init {
        initListList()
    }


    //region addList() :: Add a list to the database
    fun addList() {
        viewModelScope.launch {
            addListUseCase(
                list = EasyListsList(
                    name = state.listName,
                    notes = if (state.listNotes.isEmpty()) null else state.listNotes
                )
            )

            showAddListBottomSheet()
        }
    }
    //endregion


    //region showAddListBottomSheet()
    fun showAddListBottomSheet() {
        state = state.copy(showAddListBottomSheet = !state.showAddListBottomSheet)
    }
    //endregion


    //region addListIconButtonEnabled()
    fun addListIconButtonEnabled(): Boolean {
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


    //region onAddListBottomSheetDismiss()
    fun onAddListBottomSheetDismiss() {
        state = state.copy(
            listName = "",
            listNameInvalid = false,
            listNameInvalidMessage = "",
            showAddListBottomSheet = !state.showAddListBottomSheet,
        )
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
        cancelListFlowCollection()

        listListFlowJob = getListListFlowUseCase()
            .onEach {
                handleGetBrokerState(Resultat.success(it))
            }.catch {
                handleGetBrokerState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetBrokerState(result: Resultat<List<EasyListsList>?>) {
        result.onSuccess {
            Arbor.i("List of lists loaded successfully: $it")
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                listList = it ?: emptyList(),
            )
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
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region onActionButtonClick()
    fun onActionButtonClick(action: ListOfListsAction) {
        state = state.copy(actionButtonState = action)
        showAddListBottomSheet()
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

}
