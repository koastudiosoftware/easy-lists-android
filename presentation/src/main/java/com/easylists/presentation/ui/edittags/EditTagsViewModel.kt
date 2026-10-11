package com.easylists.presentation.ui.edittags

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.AppSettings
import com.easylists.domain.models.EasyListsList
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.use_cases.AddTagUseCase
import com.easylists.domain.use_cases.GetListFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.GetTagFlowUseCase
import com.easylists.domain.use_cases.GetTagListItemFlowUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.RemoveTagFromListItemUseCase
import com.easylists.domain.use_cases.RemoveTagUseCase
import com.easylists.domain.use_cases.UpdateTagUseCase
import com.easylists.presentation.R
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.models.ColorEditorState
import com.easylists.presentation.models.EditTagsState
import com.easylists.presentation.models.TagPendingDelete
import com.easylists.presentation.models.TagSheetState
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

private const val DEFAULT_COLOR_HEX = "#FFFFFFFF"
private val HEX_COLOR = Regex("^#[0-9A-F]{8}$")

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    getTagFlowUseCase: GetTagFlowUseCase,
    getListFlowUseCase: GetListFlowUseCase,
    getListItemFlowUseCase: GetListItemFlowUseCase,
    getTagListItemFlowUseCase: GetTagListItemFlowUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val removeTagUseCase: RemoveTagUseCase,
    private val removeTagFromListItemUseCase: RemoveTagFromListItemUseCase,
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
    var state by mutableStateOf(EditTagsState())
        private set

    private data class Snapshot(
        val tags: List<EasyListsTag>,
        val lists: List<EasyListsList>,
        val listItems: List<EasyListsListItem>,
        val tagListItems: List<TagListItem>,
    )

    init {
        // One combined collection instead of four independent ones, so the state is never
        // updated with, say, new tag/list-item links but stale list items.
        combine(
            getTagFlowUseCase(),
            getListFlowUseCase(),
            getListItemFlowUseCase(),
            getTagListItemFlowUseCase(),
        ) { tags, lists, listItems, tagListItems ->
            Snapshot(
                tags = tags.orEmpty(),
                lists = lists.orEmpty(),
                listItems = listItems.orEmpty(),
                tagListItems = tagListItems.orEmpty(),
            )
        }
            .onEach(::applySnapshot)
            .catch { state = state.copy(messageRes = R.string.error_loading_tags) }
            .launchIn(viewModelScope)
    }


    //region applySnapshot()
    private fun applySnapshot(snapshot: Snapshot) {
        val tagIds = snapshot.tags.mapNotNull { it.tagId }.toSet()

        val counts = HashMap<String, Int>()
        snapshot.tagListItems.forEach { item ->
            val id = item.tagId ?: return@forEach
            counts[id] = (counts[id] ?: 0) + 1
        }

        state = state.copy(
            tagList = snapshot.tags,
            listList = snapshot.lists,
            listItemList = snapshot.listItems,
            tagListItemList = snapshot.tagListItems,
            tagUsageCounts = counts,
            // drop anything that no longer exists (deleted here, or removed by a sync)
            selectedTagIds = state.selectedTagIds.intersect(tagIds),
            tagSheet = state.tagSheet?.takeIf {
                it.mode == AddEditMode.Add || it.tagId in tagIds
            },
            colorEditor = state.colorEditor?.takeIf { it.tagId in tagIds },
        )
    }
    //endregion


    //region messages
    fun onMessageShown() {
        state = state.copy(messageRes = null)
    }
    //endregion


    //region selection mode
    fun onTagClick(tag: EasyListsTag) {
        val id = tag.tagId ?: return
        if (state.selectionMode) {
            toggleSelection(id)
        } else {
            state = state.copy(
                tagSheet = TagSheetState(mode = AddEditMode.Edit, tagId = id, name = tag.name)
            )
        }
    }

    fun onTagLongClick(tag: EasyListsTag) {
        val id = tag.tagId ?: return
        if (!state.selectionMode) {
            state = state.copy(selectionMode = true, selectedTagIds = setOf(id))
        }
    }

    fun toggleSelection(tagId: String) {
        val ids = if (tagId in state.selectedTagIds) {
            state.selectedTagIds - tagId
        } else {
            state.selectedTagIds + tagId
        }
        state = state.copy(selectedTagIds = ids)
    }

    fun exitSelectionMode() {
        state = state.copy(selectionMode = false, selectedTagIds = emptySet())
    }
    //endregion


    //region add / edit tag sheet
    fun onAddTagClick() {
        state = state.copy(tagSheet = TagSheetState(mode = AddEditMode.Add))
    }

    fun onTagSheetDismiss() {
        state = state.copy(tagSheet = null)
    }

    fun onTagNameChange(name: String) {
        val sheet = state.tagSheet ?: return
        state = state.copy(tagSheet = sheet.copy(name = name))
    }

    fun saveTag() {
        val sheet = state.tagSheet ?: return
        if (!state.canSaveTag) return

        val name = sheet.name.trim()
        state = state.copy(tagSheet = sheet.copy(isSaving = true))

        launchCatching(
            errorRes = R.string.error_saving_tag,
            onError = {
                state.tagSheet?.let { state = state.copy(tagSheet = it.copy(isSaving = false)) }
            },
        ) {
            if (sheet.mode == AddEditMode.Add) {
                addTag(name)
            } else {
                updateTagName(sheet.tagId, name)
            }
            state = state.copy(tagSheet = null)
        }
    }

    private suspend fun addTag(name: String) {
        val ownerId = session.getUserId()
        check(ownerId.isNotEmpty()) { "No signed-in user" }

        addTagUseCase(
            easyListsTag = EasyListsTag(
                tagId = Uuid.random().toString(),
                ownerId = ownerId,
                name = name,
                isDirty = true,
            )
        ).getOrThrow()
    }

    private suspend fun updateTagName(tagId: String?, name: String) {
        val tag = state.tagList.firstOrNull { it.tagId == tagId } ?: error("Tag not found")
        updateTagUseCase(
            easyListsTag = tag.copy(
                name = name,
                isDirty = true,
                modifiedTimestamp = nowMillis(),
            )
        ).getOrThrow()
    }
    //endregion


    //region delete
    fun requestDeleteSelected() {
        if (state.selectedTagIds.isNotEmpty()) {
            state = state.copy(pendingDelete = TagPendingDelete.Selected)
        }
    }

    fun requestDeleteTag(tagId: String?) {
        if (tagId == null) return
        state = state.copy(pendingDelete = TagPendingDelete.Single(tagId))
    }

    fun onDeleteDismissed() {
        state = state.copy(pendingDelete = null)
    }

    fun onDeleteConfirmed() {
        val pending = state.pendingDelete ?: return
        val ids = when (pending) {
            is TagPendingDelete.Single -> listOf(pending.tagId)
            TagPendingDelete.Selected -> state.selectedTagIds.toList()
        }
        // hide the dialog right away so a double tap can't start a second delete
        state = state.copy(pendingDelete = null)
        if (ids.isEmpty()) return

        launchCatching(errorRes = R.string.error_deleting_tags) {
            // both delete paths clear the tag/list-item links first, then the tags themselves
            removeTagFromListItemUseCase(tagIdList = ids)
            removeTagUseCase(tagIdList = ids).getOrThrow()

            state = when (pending) {
                is TagPendingDelete.Single -> state.copy(tagSheet = null)
                TagPendingDelete.Selected -> state.copy(
                    selectionMode = false,
                    selectedTagIds = emptySet(),
                )
            }
        }
    }
    //endregion


    //region color picker
    fun onTagColorClick(tag: EasyListsTag) {
        val id = tag.tagId ?: return
        val hex = tag.color?.uppercase()?.takeIf { HEX_COLOR.matches(it) } ?: DEFAULT_COLOR_HEX
        state = state.copy(
            colorEditor = ColorEditorState(
                tagId = id,
                hexCode = hex,
                hexInput = hex.takeLast(6),
            )
        )
    }

    fun onColorEditorDismiss() {
        state = state.copy(colorEditor = null)
    }

    /** Called by the color wheel / brightness slider. [hexCode] is "AARRGGBB" without '#'. */
    fun onWheelColorChanged(hexCode: String) {
        val editor = state.colorEditor ?: return
        val hex = hexCode.removePrefix("#").uppercase()
        if (hex.length != 8) return
        state = state.copy(
            colorEditor = editor.copy(hexCode = "#$hex", hexInput = hex.takeLast(6))
        )
    }

    /** Called by the hex text field. Accepts "#RRGGBB", "RRGGBB" or a pasted "#AARRGGBB". */
    fun onHexInputChanged(raw: String) {
        val editor = state.colorEditor ?: return

        var hex = raw
            .uppercase()
            .filter { it in '0'..'9' || it in 'A'..'F' }
        if (hex.length == 8) hex = hex.drop(2)
        hex = hex.take(6)

        state = state.copy(
            colorEditor = if (hex.length == 6) {
                editor.copy(
                    hexInput = hex,
                    hexCode = "#FF$hex",
                    hexSyncCount = editor.hexSyncCount + 1,
                )
            } else {
                editor.copy(hexInput = hex)
            }
        )
    }

    fun saveTagColor() = updateSelectedTagColor(state.colorEditor?.hexCode?.uppercase())

    fun removeTagColor() = updateSelectedTagColor(null)

    private fun updateSelectedTagColor(color: String?) {
        val editor = state.colorEditor ?: return
        val tag = state.tagList.firstOrNull { it.tagId == editor.tagId } ?: return
        state = state.copy(colorEditor = null)

        launchCatching(errorRes = R.string.error_saving_tag) {
            updateTagUseCase(
                easyListsTag = tag.copy(
                    color = color,
                    isDirty = true,
                    modifiedTimestamp = nowMillis(),
                )
            )
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