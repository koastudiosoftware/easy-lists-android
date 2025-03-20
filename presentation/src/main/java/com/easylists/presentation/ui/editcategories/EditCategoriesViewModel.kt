package com.easylists.presentation.ui.editcategories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.RemoveCategoriesUseCase
import com.easylists.domain.use_cases.RemoveCategoryFromListItemUseCase
import com.easylists.domain.use_cases.UpdateCategoryUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.EditCategoriesAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.EditCategoriesState
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
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EditCategoriesViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getCategoryFlowUseCase: GetCategoryFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val removeCategoriesUseCase: RemoveCategoriesUseCase,
    private val removeCategoryFromListItemUseCase: RemoveCategoryFromListItemUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var categoryListFlowJob: Job? = null

    var state by mutableStateOf(EditCategoriesState())


    init {
        initAppSettings()
        initCategoryList()
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


    //region addCategory()
    fun addCategory() {
        viewModelScope.launch {
            addCategoryUseCase(category = EasyListsCategory(name = state.categoryName))
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
                    uid = state.selectedItem?.uid,
                    name = state.categoryName,
                    sortOrder = state.selectedItem?.sortOrder,
                    createdTimestamp = state.selectedItem?.createdTimestamp
                        ?: Instant.now().epochSecond,
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
            category.selectedForRemoval == true
        }.map { it.uid ?: "" }
        viewModelScope.launch {
            if (categoryList.isNotEmpty() || categoryList.all { it.isNotEmpty() }) {
                removeCategoryFromListItemUseCase(
                    categoryUid = categoryList,
                )
            }
            state = state.copy(nextStep = "remove_categories")
        }
    }
    //endregion


    //region removeCategories()
    fun removeCategories() {
        var categoryList = state.categoryList.filter { category ->
            category.selectedForRemoval == true
        }.map { it.uid ?: "" }

        viewModelScope.launch {
            if (categoryList.isNotEmpty() || categoryList.all { it.isNotEmpty() }) {
                removeCategoriesUseCase(uidList = categoryList)
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
            it.name.lowercase() == name.lowercase()
        } == true).let {
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

}
