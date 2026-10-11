package com.easylists.presentation.ui.categories

import androidx.annotation.StringRes
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
import com.easylists.domain.use_cases.GetCategoryUseCase
import com.easylists.domain.use_cases.GetListUseCase
import com.easylists.domain.use_cases.GetListItemUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.RemoveCategoryFromListItemUseCase
import com.easylists.domain.use_cases.DeleteCategoriesUseCase
import com.easylists.domain.use_cases.UpdateCategoryUseCase
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.models.CategoryPendingDelete
import com.easylists.presentation.models.CategorySheetState
import com.easylists.presentation.models.CategoriesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.uuid.Uuid

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    getCategoryUseCase: GetCategoryUseCase,
    getListItemUseCase: GetListItemUseCase,
    getListUseCase: GetListUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val removeCategoryFromListItemsUseCase: RemoveCategoryFromListItemUseCase,
    private val deleteCategoriesUseCase: DeleteCategoriesUseCase,
    private val session: SessionRepository,
) : ViewModel() {

    // Persisted settings. DataStore is the single source of truth.
    // null = DataStore hasn't emitted yet.
    val settings: StateFlow<AppSettings?> = observeAppSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    // Transient screen state. Only the ViewModel writes it.
    var state by mutableStateOf(CategoriesState())
        private set

    private data class CategoriesSnapshot(
        val categories: List<EasyListsCategory>,
        val lists: List<EasyListsList>,
        val listItems: List<EasyListsListItem>,
    )

    init {
        // One combined collection instead of four independent ones, so the state is never
        // updated with, say, new tag/list-item links but stale list items.
        combine(
            getCategoryUseCase(),
            getListUseCase(),
            getListItemUseCase(),
        ) { categories, lists, listItems ->
            CategoriesSnapshot(
                categories = categories.orEmpty(),
                lists = lists.orEmpty(),
                listItems = listItems.orEmpty(),
            )
        }
            .onEach(::applySnapshot)
            .catch { state = state.copy(messageRes = R.string.error_loading_categories) }
            .launchIn(viewModelScope)
    }


    //region applySnapshot()
    private fun applySnapshot(snapshot: CategoriesSnapshot) {
        val categoryIds = snapshot.categories.mapNotNull { it.categoryId }.toSet()

        val counts = HashMap<String, Int>()
        snapshot.listItems.forEach { item ->
            val id = item.categoryId ?: return@forEach
            counts[id] = (counts[id] ?: 0) + 1
        }

        state = state.copy(
            categoryList = snapshot.categories,
            listList = snapshot.lists,
            listItemList = snapshot.listItems,
            categoryUsageCounts = counts,
            // drop anything that no longer exists (deleted here, or removed by a sync)
            selectedCategoryIds = state.selectedCategoryIds.intersect(categoryIds),
            categorySheet = state.categorySheet?.takeIf {
                it.mode == AddEditMode.Add || it.categoryId in categoryIds
            },
        )
    }
    //endregion


    //region messages
    fun onMessageShown() {
        state = state.copy(messageRes = null)
    }
    //endregion


    //region selection mode
    fun onCategoryClick(category: EasyListsCategory) {
        val id = category.categoryId ?: return
        if (state.selectionMode) {
            toggleSelection(id)
        } else {
            state = state.copy(
                categorySheet = CategorySheetState(mode = AddEditMode.Edit, categoryId = id, name = category.name)
            )
        }
    }


    fun onCategoryLongClick(category: EasyListsCategory) {
        val id = category.categoryId ?: return
        if (!state.selectionMode) {
            state = state.copy(selectionMode = true, selectedCategoryIds = setOf(id))
        }
    }

    fun toggleSelection(categoryId: String) {
        val ids = if (categoryId in state.selectedCategoryIds) {
            state.selectedCategoryIds - categoryId
        } else {
            state.selectedCategoryIds + categoryId
        }
        state = state.copy(selectedCategoryIds = ids)
    }

    fun exitSelectionMode() {
        state = state.copy(selectionMode = false, selectedCategoryIds = emptySet())
    }
    //endregion


    //region add / edit tag sheet
    fun onAddCategoryClick() {
        state = state.copy(categorySheet = CategorySheetState(mode = AddEditMode.Add))
    }

    fun onCategorySheetDismiss() {
        state = state.copy(categorySheet = null)
    }

    fun onCategoryNameChange(name: String) {
        val sheet = state.categorySheet ?: return
        state = state.copy(categorySheet = sheet.copy(name = name))
    }

    fun saveCategory() {
        val sheet = state.categorySheet ?: return
        if (!state.canSaveCategory) return

        val name = sheet.name.trim()
        state = state.copy(categorySheet = sheet.copy(isSaving = true))

        launchCatching(
            errorRes = R.string.error_saving_category,
            onError = {
                state.categorySheet?.let { state = state.copy(categorySheet = it.copy(isSaving = false)) }
            },
        ) {
            if (sheet.mode == AddEditMode.Add) {
                addCategory(name)
            } else {
                updateCategoryName(sheet.categoryId, name)
            }
            state = state.copy(categorySheet = null)
        }
    }

    private suspend fun addCategory(name: String) {
        val ownerId = session.getUserId()
        check(ownerId.isNotEmpty()) { "No signed-in user" }

        addCategoryUseCase(
            category = EasyListsCategory(
                categoryId = Uuid.random().toString(),
                ownerId = ownerId,
                name = name,
                isDirty = true,
            )
        ).getOrThrow()
    }

    private suspend fun updateCategoryName(categoryId: String?, name: String) {
        val tag = state.categoryList.firstOrNull { it.categoryId == categoryId } ?: error("Tag not found")
        updateCategoryUseCase(
            category = tag.copy(
                name = name,
                isDirty = true,
                modifiedTimestamp = nowMillis(),
            )
        ).getOrThrow()
    }
    //endregion


    //region delete
    fun requestDeleteSelected() {
        if (state.selectedCategoryIds.isNotEmpty()) {
            state = state.copy(pendingDelete = CategoryPendingDelete.Selected)
        }
    }

    fun requestDeleteCategory(categoryId: String?) {
        if (categoryId == null) return
        state = state.copy(pendingDelete = CategoryPendingDelete.Single(categoryId))
    }

    fun onDeleteDismissed() {
        state = state.copy(pendingDelete = null)
    }

    fun onDeleteConfirmed() {
        val pending = state.pendingDelete ?: return
        val ids = when (pending) {
            is CategoryPendingDelete.Single -> listOf(pending.categoryId)
            CategoryPendingDelete.Selected -> state.selectedCategoryIds.toList()
        }
        // hide the dialog right away so a double tap can't start a second delete
        state = state.copy(pendingDelete = null)
        if (ids.isEmpty()) return

        launchCatching(errorRes = R.string.error_deleting_categories) {
            // both delete paths clear the tag/list-item links first, then the tags themselves
            deleteCategoriesUseCase(categoryIds = ids).getOrThrow()

            state = when (pending) {
                is CategoryPendingDelete.Single -> state.copy(categorySheet = null)
                CategoryPendingDelete.Selected -> state.copy(
                    selectionMode = false,
                    selectedCategoryIds = emptySet(),
                )
            }
        }
    }
    //endregion


    //region helpers
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    private fun launchCatching(
        @StringRes errorRes: Int,
        onError: () -> Unit = {},
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError()
                state = state.copy(messageRes = errorRes)
            }
        }
    }
    //endregion

}