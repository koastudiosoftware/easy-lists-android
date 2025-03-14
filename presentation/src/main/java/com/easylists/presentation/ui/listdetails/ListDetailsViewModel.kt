package com.easylists.presentation.ui.listdetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.use_cases.AddListFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.RemoveListUseCase
import com.easylists.presentation.common.ListOfListsAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListDetailsState
import com.easylists.presentation.models.ListListUiState
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
class ListDetailsViewModel @Inject constructor(
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val addListUseCase: AddListFlowUseCase,
    private val removeListUseCase: RemoveListUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var listItemListFlowJob: Job? = null

    var state by mutableStateOf( ListDetailsState() )


    init {
        // initAppSettings()
    }


    fun init(listUid: String) {
        state = state.copy(listUid = listUid)
        initListItemsList()
    }


    //region addListItem() :: Add a list item to the database
    fun addListItem() {
        viewModelScope.launch {
//            addListUseCase(
//                list = EasyListsList(
//                    name = state.listName,
//                    notes = if (state.listNotes.isEmpty()) null else state.listNotes
//                )
//            )
//
//            showAddListBottomSheet()
        }
    }
    //endregion


    //region removeListItem() :: remove a list item from the database
    fun removeListItem() {
        viewModelScope.launch {
//            removeListUseCase(uid = state.selectedListUid)
//            state = state.copy(selectedListUid = "")
        }
    }
    //endregion


    //region showAddListBottomSheet()
//    fun showAddListBottomSheet() {
//        state = state.copy(showAddListBottomSheet = !state.showAddListBottomSheet)
//    }
    //endregion


    //region addListIconButtonEnabled()
//    fun addListIconButtonEnabled(): Boolean {
//        return true
//    }
    //endregion


    //region listItemName()
    fun listItemName(): String {
//        return state.listItemName
        return ""
    }
    //endregion


    //region listItemsNotes()
    fun listItemNotes(): String {
//        return state.listItemNotes
        return ""
    }
    //endregion


    //region onAddListBottomSheetDismiss()
    fun onAddListBottomSheetDismiss() {
//        state = state.copy(
//            listName = "",
//            listNameInvalid = false,
//            listNameInvalidMessage = "",
//            showAddListBottomSheet = !state.showAddListBottomSheet,
//        )
    }
    //endregion


    //region onListNameChange()
    fun onListNameChange(name: String) {
//        var listNameInvalidMessage: String
//        val isNameInvalid = (state.listList?.any{ it.name == name } == true).let {
//            listNameInvalidMessage = if (it) "Name already in use" else ""
//            it
//        }
//
//        state = state.copy(
//            listName = name,
//            listNameInvalid = isNameInvalid,
//            listNameInvalidMessage = listNameInvalidMessage
//        )
    }
    //endregion


    //region onListNotesChange()
    fun onListNotesChange(notes: String) {
//        state = state.copy(listNotes = notes)
    }
    //endregion


    //region initListItemList() :: initialize list of items from the database
    fun initListItemsList() {
        cancelListItemFlowCollection()

        listItemListFlowJob = getListItemFlowUseCase(listUid = state.listUid)
            .onEach {
                handleGetListItemState(Resultat.success(it))
            }.catch {
                handleGetListItemState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListItemFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetListItemState(result: Resultat<List<EasyListsListItem>?>) {
        result.onSuccess {
            Arbor.i("List items received: $it")
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                listItemList = it?.map { item -> item } ?: emptyList(),
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


    private fun cancelListItemFlowCollection() {
        listItemListFlowJob?.cancel()
        listItemListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region onActionButtonClick()
    fun onActionButtonClick(action: ListOfListsAction) {
//        state = state.copy(actionButtonState = action)
//        showAddListBottomSheet()
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(list: EasyListsList?) {
//        if (list == null) return
//        state = state.copy(
//            selectedListUid = if (state.selectedListUid.isEmpty()) list.uid.toString() else "",
//        )
    }
    //endregion

}
