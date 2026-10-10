package com.easylists.presentation.ui.editcategories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettings
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.RemoveCategoryFromListItemUseCase
import com.easylists.domain.use_cases.RemoveCategoryUseCase
import com.easylists.domain.use_cases.UpdateCategoryUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.EditCategoriesState
import com.easylists.presentation.models.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.haan.resultat.Resultat
import fr.haan.resultat.onFailure
import fr.haan.resultat.onLoading
import fr.haan.resultat.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class EditCategoriesViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    private val getCategoryFlowUseCase: GetCategoryFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val removeCategoryUseCase: RemoveCategoryUseCase,
    private val removeCategoryFromListItemUseCase: RemoveCategoryFromListItemUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val getListListFlowUseCase: GetListFlowUseCase,
    private val session: SessionRepository,
    private val mapper: UiMapper,
) : ViewModel() {

    // Persisted settings. DataStore is the single source of truth.
    // null = DataStore hasn't emitted yet.
    val settings: StateFlow<AppSettings?> = observeAppSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    // Transient screen state
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    var userId: String = ""

    private var categoryListFlowJob: Job? = null
    private var listItemListFlowJob: Job? = null
    private var listListFlowJob: Job? = null

    var state by mutableStateOf(EditCategoriesState())


    init {
        initAppSettings()
        initCategoryList()
        initListList()
        initListItemList()
    }


    //region initAppSettings()
    fun initAppSettings() {
        viewModelScope.launch {
            // TODO we should do something more proactive if the userId cannot be fetched
            userId = session.getUserId()
            if (userId.isEmpty()) return@launch

//            val result = getAppSettingsUseCase(
//                keys = AppSettingsKeys.entries.map {
//                    mapOf(
//                        KEY to it.key,
//                        TYPE to it.type.toString()
//                    )
//                },
//            )
//
//            val capitalization =
//                result.find { it[KEY] == AppSettingsKeys.Capitalization.key }?.get(VALUE)
//
//            state = state.copy(
//                capitalization = Capitalization.from(
//                    capitalization ?: Capitalization.NoCapitalization.toString()
//                ) ?: Capitalization.NoCapitalization,
//            )
        }
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


    //region initListItemList() :: initialize list of list items from the database
    fun initListItemList() {
        cancelListItemFlowCollection()

        listItemListFlowJob = getListItemFlowUseCase()
            .onEach {
                handleGetTagListItemState(Resultat.success(it))
            }.catch {
                handleGetTagListItemState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListItemFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetTagListItemState(result: Resultat<List<EasyListsListItem>?>) {
        result.onSuccess {
            state = state.copy(
                listItemList = it?.map { item -> item } ?: emptyList(),
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


    private fun cancelListItemFlowCollection() {
        listItemListFlowJob?.cancel()
        listItemListFlowJob = null
    }
    //endregion


    //region initListList() :: initialize list of lists from the database
    fun initListList() {
        cancelListFlowCollection()

        listListFlowJob = getListListFlowUseCase()
            .onEach {
                handleGetListState(Resultat.success(it))
            }.catch {
                handleGetListState(Resultat.failure(it))

                // After this catch the flow is interrupted, and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelListFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetListState(result: Resultat<List<EasyListsList>?>) {
        result.onSuccess {
            state = state.copy(
                // TODO this is where the sorting order should be applied
                listList = it ?: emptyList(),
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


    private fun cancelListFlowCollection() {
        listListFlowJob?.cancel()
        listListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(): () -> Unit = {
        state = state.copy(isPullToRefreshing = true)
        viewModelScope.launch {
            initCategoryList()
            initListItemList()
            initListList()
            delay(500L.milliseconds) // workaround to eliminate sticky pull to refresh indicator
            state = state.copy(isPullToRefreshing = false)
        }
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
            addEditMode = AddEditMode.Edit,
            categoryName = item.name,
            selectedItem = item,
            showCategoryBottomSheet = true,
        )
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(item: EasyListsCategory? = null) {
        state.categoryList.forEach { it.selectedForRemoval = false }

        state = state.copy(
            actionButtonState = if (state.actionButtonState == EditCategoriesAction.Delete)
                EditCategoriesAction.None
            else
                EditCategoriesAction.Delete,
            selectedItem = item,
            showContextItems = !state.showContextItems
        )
    }
    //endregion


    //region onCategorySelectedForRemovalChanged()
    fun onCategorySelectedForRemovalChanged(uid: String?) {
        state = state.copy(
            categoryList = state.categoryList.map {
                if (it.categoryId == uid) {
                    it.copy(selectedForRemoval = !it.selectedForRemoval)
                } else {
                    it
                }
            }
        )
    }
    //endregion


    //region addCategory()
    fun addCategory() {
        viewModelScope.launch {
            addCategoryUseCase(category = EasyListsCategory(
                ownerId = userId,
                name = state.categoryName
            ))
            state = state.copy(
                categoryName = "",
                categoryNameInvalid = false,
                categoryNameInvalidMessage = "",
                showCategoryBottomSheet = false
            )
        }
    }
    //endregion


    //region updateCategory()
    fun updateCategory() {
        viewModelScope.launch {
            updateCategoryUseCase(
                category = EasyListsCategory(
                    categoryId = state.selectedItem?.categoryId,
                    ownerId = state.selectedItem?.ownerId ?: "",
                    name = state.categoryName,
                    sortOrder = state.selectedItem?.sortOrder,
                    isDirty = true,
                    isDeleted = false,
                    createdTimestamp = state.selectedItem?.createdTimestamp
                        ?: Clock.System.now().toEpochMilliseconds(),
                )
            )

            state = state.copy(
                categoryName = "",
                categoryNameInvalid = false,
                categoryNameInvalidMessage = "",
                showCategoryBottomSheet = false
            )
        }
    }
    //endregion


    //region removeCategoryFromListItems()
    fun removeCategoryFromListItems() {
        val categoryList = state.categoryList.filter { category ->
            category.selectedForRemoval
        }.map { it.categoryId ?: "" }
        viewModelScope.launch {
            if (categoryList.isNotEmpty() || categoryList.all { it.isNotEmpty() }) {
                removeCategoryFromListItemUseCase(
                    categoryIdList = categoryList,
                )
            }
            state = state.copy(nextStep = "remove_categories")
        }
    }
    //endregion


    //region removeCategories()
    fun removeCategories() {
        val categoryList = state.categoryList.filter { category ->
            category.selectedForRemoval
        }.map { it.categoryId ?: "" }

        viewModelScope.launch {
            if (categoryList.isNotEmpty() || categoryList.all { it.isNotEmpty() }) {
                removeCategoryUseCase(categoryIds = categoryList)
                state.categoryList.forEach { it.selectedForRemoval = false }
            }

            state = state.copy(
                deselectCheckboxes = false,
                nextStep = ""
            )
        }
    }
    //endregion


    //region deselectCheckboxes()
    fun deselectCheckboxes() {
        state = state.copy(deselectCheckboxes = true)
    }
    //endregion


    //region categoryIconButtonEnabled()
    fun categoryIconButtonEnabled(): Boolean {
        return state.categoryName.isNotEmpty() &&
                state.categoryList.all { it.name != state.categoryName }
    }
    //endregion


    //region categoryName()
    fun categoryName(): String {
        return state.categoryName
    }
    //endregion


    //region onCategoryNameChange()
    fun onCategoryNameChange(name: String) {
        var categoryNameInvalidMessage: String
        val isNameInvalid = (state.categoryList.any {
            it.name.equals(name, ignoreCase = true)
        }).let {
            categoryNameInvalidMessage = if (it) "Name already in use" else ""
            it
        }

        state = state.copy(
            categoryName = name,
            categoryNameInvalid = isNameInvalid,
            categoryNameInvalidMessage = categoryNameInvalidMessage
        )
    }
    //endregion


    //region onCategoryBottomSheetDismiss()
    fun onCategoryBottomSheetDismiss() {
        state = state.copy(
            addEditMode = AddEditMode.Add,
            categoryName = "",
            categoryNameInvalid = false,
            categoryNameInvalidMessage = "",
            showCategoryBottomSheet = !state.showCategoryBottomSheet,
        )
    }
    //endregion


    //region categoryListItemCount()
    fun categoryListItemCount(category: EasyListsCategory): Int {
        return state.listItemList.count { it.categoryId == category.categoryId }
    }
    //endregion


    //region listItems()
    fun listItems(): List<EasyListsListItem> {
        return state.listItemList.filter { it.categoryId == state.selectedItem?.categoryId }
    }
    //endregion


    //region lists()
    fun lists(): List<EasyListsList> {
        val listItems = listItems()
        return state.listList.filter { list ->
            listItems.any { it.listId == list.listId }
        }
    }
    //endregion


    //region dismissConfirmationDialog()
    fun dismissConfirmationDialog() {
        state = state.copy(
            confirmationTitle = "",
            confirmationMessage = "",
            confirmationOnConfirmation = {},
            confirmationOnDismissRequest = {},
            showConfirmationDialog = false,
        )
    }
    //endregion


    //region configureRemoveCategory
    fun configureDeleteCategory(
        title: String,
        message: String,
        onConfirmation: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        state = state.copy(
            confirmationTitle = title,
            confirmationMessage = message,
            confirmationOnConfirmation = onConfirmation,
            confirmationOnDismissRequest = onDismissRequest,
        )

        setShowConfirmationDialogState(true)
    }
    //endregion

}
