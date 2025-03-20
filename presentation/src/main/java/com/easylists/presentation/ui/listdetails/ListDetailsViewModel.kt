package com.easylists.presentation.ui.listdetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.AddListItemFlowUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.RemoveListItemUseCase
import com.easylists.domain.use_cases.UpdateListItemFlowUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.isNumeric
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ListDetailsState
import com.easylists.presentation.models.ListListUiState
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
import java.time.Instant
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val getCategoryFlowUseCase: GetCategoryFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val addListItemUseCase: AddListItemFlowUseCase,
    private val updateListItemUseCase: UpdateListItemFlowUseCase,
    private val removeListItemUseCase: RemoveListItemUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var categoryListFlowJob: Job? = null
    private var listItemListFlowJob: Job? = null

    var state by mutableStateOf( ListDetailsState() )


    init {
        initAppSettings()
    }


    //region init
    fun init(listUid: String, listName: String) {
        state = state.copy(
            listName = listName,
            listUid = listUid
        )
    }
    //endregion


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

            val groupCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.GroupCrossedOffItems.key }?.get(VALUE)

            val sortCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.SortCrossedOffItems.key }?.get(VALUE)

            state = state.copy(
                capitalization = Capitalization.from(
                    capitalization ?: Capitalization.NoCapitalization.toString()
                ) ?: Capitalization.NoCapitalization,

                groupCrossedOffItems = GroupCrossedOffItems.from(
                    groupCrossedOffItems ?: GroupCrossedOffItems.AllTogether.toString()
                ) ?: GroupCrossedOffItems.AllTogether,

                sortCrossedOffItems = SortCrossedOffItems.from(
                    sortCrossedOffItems ?: SortCrossedOffItems.MostRecentOnTop.toString()
                ) ?: SortCrossedOffItems.MostRecentOnTop,
            )
        }
    }
    //endregion


    //region addListItem() :: Add a list item to the database
    @OptIn(ExperimentalUuidApi::class)
    fun addListItem() {
        viewModelScope.launch {
            var category: EasyListsCategory

            var categoryUid = state.categoryList.find { it.name == state.categoryText }?.uid
            if (categoryUid == null && state.categoryText.isNotEmpty()) {
                categoryUid = Uuid.random().toString()
                category = EasyListsCategory(
                    uid = categoryUid,
                    name = state.categoryText,
                )
                addCategoryUseCase(category)
            }

            delay(100L)     // allow a short time for the category to be added to the database
            var listItem = EasyListsListItem(
                listUid = state.listUid,
                name = state.itemName,
                categoryUid = categoryUid,
                notes = if (state.itemNotes.isEmpty()) null else state.itemNotes,
                quantity = if (state.itemQuantity.isEmpty()) null else state.itemQuantity.toInt(),
            )

            if (state.addEditMode == AddEditMode.Add) {
                addListItemUseCase(listItem = listItem)
            } else {
                listItem.uid = state.itemUid
                updateListItemUseCase(listItem = listItem)
            }

            showListItemBottomSheet()
            state = state.copy(
                categoryText = "",
                itemName = "",
                itemNameInvalid = false,
                itemNameInvalidMessage = "",
                itemNotes = "",
                itemQuantity = "",
                selectedCategoryIndex = -1,
            )
        }
    }
    //endregion


    //region removeListItem() :: remove a list item from the database
    fun removeListItem() {
        viewModelScope.launch {
            removeListItemUseCase(uid = state.selectedItemUid)
            state = state.copy(selectedItemUid = "")
        }
    }
    //endregion


    //region showListItemBottomSheet()
    fun showListItemBottomSheet() {
        state = state.copy(showListItemBottomSheet = !state.showListItemBottomSheet)
    }
    //endregion


    //region itemName()
    fun itemName(): String {
        return state.itemName
    }
    //endregion


    //region itemNotes()
    fun itemNotes(): String {
        return state.itemNotes
    }
    //endregion


    //region itemQuantity()
    fun itemQuantity(): String {
        return state.itemQuantity
    }
    //endregion


    //region listItemIconButtonEnabled()
    fun listItemIconButtonEnabled(): Boolean {
        if (state.itemName.isEmpty()) return false

        if (state.addEditMode == AddEditMode.Edit) return true

        // don't allow duplicate item name
        if (state.listItemList.any { it.name.lowercase() == state.itemName.lowercase() }) return false

        return true
    }
    //endregion


    //region onItemBottomSheetDismiss()
    fun onItemBottomSheetDismiss() {
        state = state.copy(
            addEditMode = AddEditMode.Add,
            itemUid = "",
            itemName = "",
            itemNotes = "",
            itemQuantity = "",
            itemNameInvalid = false,
            itemNameInvalidMessage = "",
            selectedCategoryIndex = -1,
            showListItemBottomSheet = !state.showListItemBottomSheet,
        )
    }
    //endregion


    //region categoryFromIndex()
    fun categoryFromIndex(): String {
        if (state.selectedCategoryIndex < 0) return state.categoryText
        return state.categoryList[state.selectedCategoryIndex].name
    }
    //endregion


    //region onCategoryChange()
    fun onCategoryChange(index: Int) {
        state = state.copy(
            categoryText = state.categoryList[index].name,
            selectedCategoryIndex = index
        )
    }


    fun onCategoryChange(newCategory: String) {
        state = state.copy(categoryText = newCategory)
    }
    //endregion


    //region onItemNameChange()
    fun onItemNameChange(name: String) {
        var itemNameInvalidMessage: String
        val isNameInvalid = (state.listItemList.any {
            it.name.lowercase() == name.lowercase()
        } == true).let {
            itemNameInvalidMessage = if (it) "Name already in use" else ""
            it
        }

        state = state.copy(
            itemName = name,
            itemNameInvalid = isNameInvalid,
            itemNameInvalidMessage = itemNameInvalidMessage
        )
    }
    //endregion


    //region onItemQuantityChange()
    fun onItemQuantityChange(quantity: String) {
        if (isNumeric(quantity)) {
            state = state.copy(itemQuantity = quantity)
        }
    }
    //endregion


    //region onItemNotesChange()
    fun onItemNotesChange(notes: String) {
        state = state.copy(itemNotes = notes)
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
            var groupedItemList: Map<Pair<Boolean?, String?>, List<EasyListsListItem>>? = null

            if (state.categoryList.isNotEmpty() == true) {
                // apply category to each item pulled from the database
                it?.forEach {
                    it.category = state.categoryList.find { category ->
                        category.uid == it.categoryUid
                    }?.name ?: "Uncategorized"
                }

                // group all items first by crossedOff then by category
                groupedItemList = it?.map { item -> item }?.sortedBy {
                    it.category
                }?.groupBy {
                    Pair(it.crossedOff, it.category)
                }
            }

            state = state.copy(
                isPullToRefreshing = false,
                groupedItemList = groupedItemList,
                listItemList = it?.map { item -> item } ?: emptyList(),
                nextDataFetchStage = "",
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
                nextDataFetchStage = "item",
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


    private fun cancelCategoryFlowCollection() {
        categoryListFlowJob?.cancel()
        categoryListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(item: EasyListsListItem?) {
        if (item == null) return
        state = state.copy(
            selectedItemUid = if (state.selectedItemUid.isEmpty()) item.uid.toString() else "",
        )
    }
    //endregion


    //region onListItemClick
    fun onListItemClick(item: EasyListsListItem) {
        // update the item
        item.crossedOff = !item.crossedOff!!
        item.crossedOffTimestamp = Instant.now().epochSecond
        viewModelScope.launch {
            updateListItemUseCase(item)
        }
    }
    //endregion


    //region onListItemInfoClick()
    fun onListItemInfoClick(item: EasyListsListItem, addEditMode: AddEditMode) {
        val category = state.categoryList.find { it.uid == item.categoryUid }
        val index = state.categoryList.indexOf(category)

        state = state.copy(
            addEditMode = addEditMode,
            categoryText = category?.name ?: "",
            itemName = item.name,
            itemNotes = item.notes ?: "",
            itemQuantity = item.quantity?.toString() ?: "",
            itemUid = item.uid.toString(),
            selectedCategoryIndex = index,
            showListItemBottomSheet = true
        )
    }
    //endregion


    //region deleteAllCrossedOffItems()
    fun deleteAllCrossedOffItems() {
        viewModelScope.launch {
            state.listItemList.filter { it.crossedOff == true }.forEach {
                removeListItemUseCase(it.uid.toString())
            }
        }
    }
    //endregion


    //region setShowConfirmationDialogState()
    fun setShowConfirmationDialogState(newState: Boolean) {
        state = state.copy(showConfirmationDialog = newState)
    }
    //endregion

}
