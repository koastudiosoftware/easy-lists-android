package com.easylists.presentation.ui.editcategories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.UpdateListItemFlowUseCase
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.EditCategoriesState
import com.easylists.presentation.models.ListListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.haan.resultat.Resultat
import fr.haan.resultat.onFailure
import fr.haan.resultat.onLoading
import fr.haan.resultat.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EditCategoriesViewModel @Inject constructor(
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val getCategoryFlowUseCase: GetCategoryFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateListItemUseCase: UpdateListItemFlowUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var categoryListFlowJob: Job? = null
    private var listItemListFlowJob: Job? = null

    var state by mutableStateOf(EditCategoriesState())


    init {
        initCategoryList()
    }


    //region initCategoryList() :: initialize list of items from the database
    fun initCategoryList() {
        cancelCategoryFlowCollection()

        categoryListFlowJob = getCategoryFlowUseCase()
            .onEach {
                handleGetCategoryState(Resultat.success(it))
            }.catch {
                handleGetCategoryState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelCategoryFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetCategoryState(result: Resultat<List<EasyListsCategory>?>) {
        result.onSuccess {
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                categoryList = it?.map { item -> item } ?: emptyList(),
            )
        }.onFailure {
//            state = state.copy(
//                uiState = ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))
//            )
        }.onLoading {
//            state = state.copy(
//                state = CoinsListUiState.Refreshing(isAutomaticRefresh = true)
//            )
        }
    }


    private fun cancelCategoryFlowCollection() {
        categoryListFlowJob?.cancel()
        categoryListFlowJob = null
    }
    //endregion


//    //region initListItemList() :: initialize list of items from the database
//    fun initListItemsList() {
//        cancelListItemFlowCollection()
//
//        listItemListFlowJob = getListItemFlowUseCase(categoryUid = state.listUid)
//            .onEach {
//                handleGetListItemState(Resultat.success(it))
//            }.catch {
//                handleGetListItemState(Resultat.failure(it))
//
//                // After this catch the flow is interrupted and it must be collected
//                // again to obtain new data. The handleRefresh() method handles this situation.
//                cancelListItemFlowCollection()
//            }.launchIn(viewModelScope)
//    }
//
//
//    private fun handleGetListItemState(result: Resultat<List<EasyListsListItem>?>) {
//        result.onSuccess {
//            var groupedItemList: Map<Pair<Boolean?, String?>, List<EasyListsListItem>>? = null
//
//            if (state.categoryList.isNotEmpty() == true) {
//                // apply category to each item pulled from the database
//                it?.forEach {
//                    it.category = state.categoryList.find { category ->
//                        category.uid == it.categoryUid
//                    }?.name ?: "Uncategorized"
//                }
//
//                // group all items first by crossedOff then by category
//                groupedItemList = it?.map { item -> item }?.sortedBy {
//                    it.category
//                }?.groupBy {
//                    Pair(it.crossedOff, it.category)
//                }
//            }
//
//            state = state.copy(
//                isPullToRefreshing = false,
//                groupedItemList = groupedItemList,
//                listItemList = it?.map { item -> item } ?: emptyList(),
//                nextDataFetchStage = "",
//            )
//        }.onFailure {
//            state = state.copy(
//                uiState = ListListUiState.Error(message = mapper.mapErrorToUiMessage(it))
//            )
//        }.onLoading {
////            state = state.copy(
////                state = CoinsListUiState.Refreshing(isAutomaticRefresh = true)
////            )
//        }
//    }
//
//
//    private fun cancelListItemFlowCollection() {
//        listItemListFlowJob?.cancel()
//        listItemListFlowJob = null
//    }
//    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region showCategoryBottomSheet()
    fun showCategoryBottomSheet() {
        state = state.copy(showCategoryBottomSheet = !state.showCategoryBottomSheet)
    }
    //endregion


    //region setShowConfirmationDialogState()
    fun setShowConfirmationDialogState(newState: Boolean) {
        state = state.copy(showConfirmationDialog = newState)
    }
    //endregion


    //region onCategoryClick()
    fun onCategoryClick(item: EasyListsCategory) {
        state = state.copy(
            selectedItem = item,
            showCategoryBottomSheet = true,
        )
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(item: EasyListsCategory? = null) {
        state.categoryList.forEach { it.selectedForRemoval = false }

        state = state.copy(
            actionButtonState = if (state.actionButtonState == EditCategoriesAction.Remove)
                EditCategoriesAction.None
            else
                EditCategoriesAction.Remove,
            selectedItem = item,
            showContextItems = !state.showContextItems
        )
    }
    //endregion


    //region onCategorySelectedForRemovalChanged()
    fun onCategorySelectedForRemovalChanged(uid: String?) {
        state = state.copy(
            categoryList = state.categoryList.map {
                if (it.uid == uid) {
                    it.copy(selectedForRemoval = !it.selectedForRemoval)
                } else {
                    it
                }
            }
        )
    }
    //endregion

}