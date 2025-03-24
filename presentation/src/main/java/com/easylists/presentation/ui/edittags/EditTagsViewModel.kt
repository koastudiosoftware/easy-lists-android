package com.easylists.presentation.ui.edittags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.domain.use_cases.AddTagUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.GetTagFlowUseCase
import com.easylists.domain.use_cases.GetTagListItemFlowUseCase
import com.easylists.domain.use_cases.RemoveTagFromListItemUseCase
import com.easylists.domain.use_cases.RemoveTagUseCase
import com.easylists.domain.use_cases.UpdateTagUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.EditTagsAction
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.EditTagsState
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
import java.time.Instant
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getTagFlowUseCase: GetTagFlowUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val removeTagUseCase: RemoveTagUseCase,
    private val removeTagFromListItemUseCase: RemoveTagFromListItemUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val getTagListItemFlowUseCase: GetTagListItemFlowUseCase,
    private val getListListFlowUseCase: GetListFlowUseCase,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var listItemListFlowJob: Job? = null
    private var listListFlowJob: Job? = null
    private var tagListFlowJob: Job? = null
    private var tagListItemFlowJob: Job? = null

    var state by mutableStateOf( EditTagsState() )


    init {
        initAppSettings()
        initTagList()
        initListList()
        initListItemList()
        initTagListItemList()
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


    //region initTagList() :: initialize list of tags from the database
    fun initTagList() {
        cancelTagFlowCollection()

        tagListFlowJob = getTagFlowUseCase()
            .onEach {
                handleGetTagState(Resultat.success(it))
            }.catch {
                handleGetTagState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelTagFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetTagState(result: Resultat<List<EasyListsTag>?>) {
        result.onSuccess {
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                tagList = it?.map { item -> item } ?: emptyList(),
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


    private fun cancelTagFlowCollection() {
        tagListFlowJob?.cancel()
        tagListFlowJob = null
    }
    //endregion


    //region initTagListItemList() :: initialize list of tag list items from the database
    fun initTagListItemList() {
        cancelTagListItemFlowCollection()

        tagListItemFlowJob = getTagListItemFlowUseCase()
            .onEach {
                handleGetTagListItemState(Resultat.success(it))
            }.catch {
                handleGetTagListItemState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelTagListItemFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetTagListItemState(result: Resultat<List<TagListItem>?>) {
        result.onSuccess {
            state = state.copy(
                isPullToRefreshing = false,
                // TODO this is where the sorting order should be applied
                tagListItemList = it?.map { item -> item } ?: emptyList(),
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


    private fun cancelTagListItemFlowCollection() {
        tagListItemFlowJob?.cancel()
        tagListItemFlowJob = null
    }
    //endregion


    //region initListItemList() :: initialize list of list items from the database
    fun initListItemList() {
        cancelListItemFlowCollection()

        listItemListFlowJob = getListItemFlowUseCase()
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
            state = state.copy(
                isPullToRefreshing = false,
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
    fun onPullToRefresh(isRefreshing: Boolean): () -> Unit = {
        state = state.copy(isPullToRefreshing = isRefreshing)
    }
    //endregion


    //region showTagBottomSheet()
    fun showTagBottomSheet() {
        state = state.copy(showTagBottomSheet = !state.showTagBottomSheet)
    }
    //endregion


    //region setShowConfirmationDialogState()
    fun setShowConfirmationDialogState(newState: Boolean) {
        state = state.copy(showConfirmationDialog = newState)
    }
    //endregion


    //region onTagClick()
    fun onTagClick(item: EasyListsTag) {
        state = state.copy(
            addEditMode = AddEditMode.Edit,
            tagName = item.name,
            selectedItem = item,
            showTagBottomSheet = true,
        )
    }
    //endregion


    //region showContextIcons()
    fun showContextIcons(item: EasyListsTag? = null) {
        state.tagList.forEach { it.selectedForRemoval = false }

        state = state.copy(
            actionButtonState = if (state.actionButtonState == EditTagsAction.Remove)
                EditTagsAction.None
            else
                EditTagsAction.Remove,
            selectedItem = item,
            showContextItems = !state.showContextItems
        )
    }
    //endregion


    //region onTagSelectedForRemovalChanged()
    fun onTagSelectedForRemovalChanged(uid: String?) {
        state = state.copy(
            tagList = state.tagList.map {
                if (it.uid == uid) {
                    it.copy(selectedForRemoval = !it.selectedForRemoval)
                } else {
                    it
                }
            }
        )
    }
    //endregion


    //region addTag()
    @OptIn(ExperimentalUuidApi::class)
    fun addTag() {
        viewModelScope.launch {
            addTagUseCase(easyListsTag = EasyListsTag(
                uid = Uuid.random().toString(),
                name = state.tagName
            ))
            state = state.copy(
                tagName = "",
                tagNameInvalid = false,
                tagNameInvalidMessage = "",
                showTagBottomSheet = false
            )
        }
    }
    //endregion


    //region updateTag()
    fun updateTag() {
        viewModelScope.launch {
            updateTagUseCase(
                easyListsTag = EasyListsTag(
                    uid = state.selectedItem?.uid,
                    name = state.tagName,
                    createdTimestamp = state.selectedItem?.createdTimestamp
                        ?: Instant.now().epochSecond,
                )
            )

            state = state.copy(
                tagName = "",
                tagNameInvalid = false,
                tagNameInvalidMessage = "",
                showTagBottomSheet = false
            )
        }
    }
    //endregion


    //region removeTagFromListItems()
    fun removeTagFromListItems() {
        val tagList = state.tagList.filter { category ->
            category.selectedForRemoval == true
        }.map { it.uid ?: "" }
        viewModelScope.launch {
            if (tagList.isNotEmpty() || tagList.all { it.isNotEmpty() }) {
                removeTagFromListItemUseCase(
                    tagUid = tagList,
                )
            }
            state = state.copy(nextStep = "remove_tags")
        }
    }
    //endregion


    //region removeTags()
    fun removeTags() {
        var tagList = state.tagList.filter { tag ->
            tag.selectedForRemoval == true
        }.map { it.uid ?: "" }

        viewModelScope.launch {
            if (tagList.isNotEmpty() || tagList.all { it.isNotEmpty() }) {
                removeTagUseCase(uidList = tagList)
                state.tagList.forEach { it.selectedForRemoval = false }
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


    //region tagIconButtonEnabled()
    fun tagIconButtonEnabled(): Boolean {
        return state.tagName.isNotEmpty() &&
                state.tagList.all { it.name != state.tagName }
    }
    //endregion


    //region tagName()
    fun tagName(): String {
        return state.tagName
    }
    //endregion


    //region onTagNameChange()
    fun onTagNameChange(name: String) {
        var tagNameInvalidMessage: String
        val isNameInvalid = (state.tagList.any {
            it.name.lowercase() == name.lowercase()
        } == true).let {
            tagNameInvalidMessage = if (it) "Name already in use" else ""
            it
        }

        state = state.copy(
            tagName = name,
            tagNameInvalid = isNameInvalid,
            tagNameInvalidMessage = tagNameInvalidMessage
        )
    }
    //endregion


    //region onTagBottomSheetDismiss()
    fun onTagBottomSheetDismiss() {
        state = state.copy(
            addEditMode = AddEditMode.Add,
            selectedItem = null,
            showTagBottomSheet = !state.showTagBottomSheet,
            tagName = "",
            tagNameInvalid = false,
            tagNameInvalidMessage = "",
        )
    }
    //endregion


    //region tagItemCount()
    fun tagListItemCount(tag: EasyListsTag): Int {
        return state.tagListItemList.count { it.tagUid == tag.uid }
    }
    //endregion


    //region listItems()
    fun listItems(): List<EasyListsListItem> {
        val tagListItemList = state.tagListItemList.filter {
            it.tagUid == state.selectedItem?.uid
        }

        val listItems = state.listItemList.filter {
            tagListItemList.any { tagListItem ->
                tagListItem.listItemUid == it.uid
            }
        }
        return listItems
    }
    //endregion


    //region lists()
    fun lists(): List<EasyListsList> {
        val listItems = listItems()
        return state.listList.filter {
            listItems.any { listItem -> it.uid == listItem.listUid }
        }
    }
    //endregion


    //region dismissConfirmationDialog()
    fun dismissConfirmationDialog() {
        state = state.copy(
            actionButtonState = EditTagsAction.None,
            confirmationTitle = "",
            confirmationMessage = "",
            confirmationOnConfirmation = {},
            confirmationOnDismissRequest = {},
            showConfirmationDialog = false,
            showTagBottomSheet = false,
            tagName = "",
            tagNameInvalid = false,
            tagNameInvalidMessage = "",
        )
    }
    //endregion


    //region configureRemoveTag
    fun configureRemoveTag(
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
