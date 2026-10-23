package com.easylists.presentation.ui.listitems

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.use_cases.AddCategoryForListUseCase
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.AddListItemUseCase
import com.easylists.domain.use_cases.AddTagListItemUseCase
import com.easylists.domain.use_cases.DeleteListItemsUseCase
import com.easylists.domain.use_cases.GetCategoryUseCase
import com.easylists.domain.use_cases.GetListItemUseCase
import com.easylists.domain.use_cases.GetTagListItemUseCase
import com.easylists.domain.use_cases.GetTagUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.RemoveTagListItemUseCase
import com.easylists.domain.use_cases.RestoreCrossedOffListItemsUseCase
import com.easylists.domain.use_cases.SaveListItemPhotoUseCase
import com.easylists.domain.use_cases.UndeleteListItemsUseCase
import com.easylists.domain.use_cases.UpdateListItemUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ItemEditorState
import com.easylists.presentation.models.ListItemsEvent
import com.easylists.presentation.models.ListItemsInteractionState
import com.easylists.presentation.models.ListItemsUiState
import com.easylists.presentation.models.PhotoDraft
import com.easylists.presentation.models.PhotoTransform
import com.easylists.presentation.models.validate
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class ListItemsViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    getListItemUseCase: GetListItemUseCase,
    getCategoryUseCase: GetCategoryUseCase,
    getTagUseCase: GetTagUseCase,
    getTagListItemUseCase: GetTagListItemUseCase,
    private val addCategoryForListUseCase: AddCategoryForListUseCase,    private val addTagListItemUseCase: AddTagListItemUseCase,
    private val removeTagListItemUseCase: RemoveTagListItemUseCase,
    private val addListItemUseCase: AddListItemUseCase,
    private val updateListItemUseCase: UpdateListItemUseCase,
    private val restoreCrossedOffListItemsUseCase: RestoreCrossedOffListItemsUseCase,
    private val deleteListItemUseCase: DeleteListItemsUseCase,
    private val saveListItemPhotoUseCase: SaveListItemPhotoUseCase,
    private val undeleteListItemsUseCase: UndeleteListItemsUseCase,
    private val session: SessionRepository,
    private val mapper: UiMapper,
) : ViewModel() {

    private val listId = MutableStateFlow<String?>(null)
    private val refreshTrigger = MutableStateFlow(0)

    private val _events = Channel<ListItemsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    var listName by mutableStateOf("")
        private set

    // Everything the list shows comes from one combined flow. A change to the items,
    // categories, tags, tag links, or settings rebuilds the rows, so nothing is copied
    // into state and no fetch ordering or delay is needed.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListItemsUiState> =
        combine(listId.filterNotNull(), refreshTrigger) { id, _ -> id }
            .flatMapLatest { id ->
                combine(
                    getListItemUseCase(listId = id),
                    getCategoryUseCase(listId = id),
                    getTagUseCase(),
                    getTagListItemUseCase(),
                    observeAppSettings(),
                ) { items, categories, tags, tagLinks, settings ->
                    buildListDetailsState(
                        items = items.orEmpty(),
                        categories = categories.orEmpty(),
                        tags = tags.orEmpty(),
                        tagLinks = tagLinks.orEmpty(),
                        settings = settings,
                    )
                }.catch { emit(ListItemsUiState.Error(message = mapper.mapErrorToUiMessage(it))) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListItemsUiState.Loading)

    // null = bottom sheet closed. Compose state so the TextFields read and write synchronously.
    var editor by mutableStateOf<ItemEditorState?>(null)
        private set

    var interaction by mutableStateOf(ListItemsInteractionState())
        private set

    private val successState: ListItemsUiState.Success?
        get() = uiState.value as? ListItemsUiState.Success


    //region init
    fun init(listUid: String, listName: String) {
        this.listName = listName
        listId.value = listUid
    }
    //endregion


    //region Editor
    private fun updateEditor(transform: (ItemEditorState) -> ItemEditorState) {
        editor = editor?.let(transform)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddItemClick() {
        editor = ItemEditorState(itemId = Uuid.generateV7().toString())
    }

    fun onEditItemClick(item: EasyListsListItem) {
        val itemId = item.listItemId ?: return
        val state = successState

        val categoryName = item.categoryId
            ?.let { id -> state?.categories?.find { it.categoryId == id }?.name }
            .orEmpty()

        val selectedTagIds = state?.tagLinks.orEmpty()
            .filter { it.listItemId == itemId }
            .map { it.tagId }
            .toSet()

        val photo = item.photoUri?.takeIf { it.isNotEmpty() }?.let { uri ->
            PhotoDraft(
                uri = uri,
                initial = PhotoTransform(
                    scale = (item.photoScale?.toFloat() ?: 1f).takeIf { it > 0f } ?: 1f,
                    offsetX = item.photoOffsetX?.toFloat() ?: 0f,
                    offsetY = item.photoOffsetY?.toFloat() ?: 0f,
                ),
            )
        }

        editor = ItemEditorState(
            itemId = itemId,
            original = item,
            name = item.name,
            quantity = item.quantity?.toString().orEmpty(),
            notes = item.notes.orEmpty(),
            categoryName = categoryName,
            selectedTagIds = selectedTagIds,
            photo = photo,
        )
    }

    fun onEditorDismiss() {
        editor = null
    }

    fun onItemNameChange(name: String) = updateEditor { it.copy(name = name) }

    fun onItemNotesChange(notes: String) = updateEditor { it.copy(notes = notes) }

    fun onCategoryChange(name: String) = updateEditor { it.copy(categoryName = name) }

    // digits only, at most 9 of them, so the value always fits in an Int
    fun onItemQuantityChange(quantity: String) {
        if (quantity.length <= 9 && quantity.all { it in '0'..'9' }) {
            updateEditor { it.copy(quantity = quantity) }
        }
    }

    fun onTagClick(tag: EasyListsTag) {
        val tagId = tag.tagId ?: return
        updateEditor {
            it.copy(
                selectedTagIds =
                    if (tagId in it.selectedTagIds) it.selectedTagIds - tagId
                    else it.selectedTagIds + tagId
            )
        }
    }

    fun onPhotoChosen(uri: String) = updateEditor { it.copy(photo = PhotoDraft(uri = uri)) }

    fun onPhotoTransformChanged(scale: Float, offsetX: Float, offsetY: Float) = updateEditor { e ->
        val photo = e.photo ?: return@updateEditor e
        e.copy(photo = photo.copy(current = PhotoTransform(scale, offsetX, offsetY)))
    }

    fun onPhotoRemove() = updateEditor { it.copy(photo = null) }
    //endregion


    //region saveListItem()
    fun saveListItem(
        uncategorizedLabel: String,
        onSaved: () -> Unit
    ) {
        val form = editor ?: return
        val current = successState ?: return
        val currentListId = listId.value ?: return
        if (!form.validate(current.items).canSave) return

        viewModelScope.launch {
            // photo: only a newly chosen image is copied to app-private storage
            val photo = form.photo
            val photoUri = when {
                photo == null -> null
                photo.uri == form.original?.photoUri -> photo.uri
                else -> saveNewPhoto(photo.uri) ?: form.original?.photoUri
            }

            // category: reuse a matching one, otherwise create it
            val categoryName = form.categoryName.trim()
                .takeUnless { it.equals(uncategorizedLabel, ignoreCase = true) }
                .orEmpty()
            val categoryId: String? = if (categoryName.isEmpty()) {
                null
            } else {
                current.categories
                    .find { it.name.equals(categoryName, ignoreCase = true) }?.categoryId
                    ?: createCategory(categoryName)
                    ?: return@launch
            }

            // tag links are written before the item, as before
            syncTagLinks(form.itemId, form.selectedTagIds, current.tagLinks)

            // an edit starts from the existing item so crossed-off state, sort order,
            // and created timestamp are preserved
            val item = (form.original ?: EasyListsListItem(
                listItemId = form.itemId,
                listId = currentListId,
                name = form.name,
            )).copy(
                name = form.name.trim(),
                categoryId = categoryId,
                notes = form.notes.ifEmpty { null },
                quantity = form.quantity.toIntOrNull(),
                photoUri = photoUri,
                photoScale = photo?.current?.scale?.toDouble(),
                photoOffsetX = photo?.current?.offsetX?.toDouble(),
                photoOffsetY = photo?.current?.offsetY?.toDouble(),
                isDirty = true,
                isDeleted = false,
                modifiedTimestamp = Clock.System.now().toEpochMilliseconds(),
            )

            if (form.mode == AddEditMode.Add) {
                addListItemUseCase(listItem = item)
            } else {
                updateListItemUseCase(listItem = item)
            }

            onSaved()
        }
    }
    //endregion


    private suspend fun createCategory(name: String): String? {
        val currentListId = listId.value ?: return null
        return addCategoryForListUseCase(currentListId, name).getOrNull()
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun syncTagLinks(
        itemId: String,
        selected: Set<String>,
        links: List<TagListItem>,
    ) {
        val existing = links.filter { it.listItemId == itemId }.map { it.tagId }.toSet()
        val toRemove = existing - selected
        val toAdd = selected - existing

        if (toRemove.isNotEmpty()) {
            removeTagListItemUseCase(listItemId = itemId, tagIdList = toRemove.toList())
        }
        if (toAdd.isNotEmpty()) {
            addTagListItemUseCase(
                tagListItem = toAdd.map {
                    TagListItem(
                        tagListItemId = Uuid.generateV7().toString(),
                        listItemId = itemId,
                        tagId = it,
                    )
                }
            )
        }
    }

    // copies the chosen image to app-private storage and returns its path
    // asks the repository to store the chosen image and returns the value to keep in photo_uri
    private suspend fun saveNewPhoto(sourceUri: String): String? {
        // TODO surface an error to the user when the image can't be loaded or saved
        return try {
            saveListItemPhotoUseCase(sourceUri).getOrThrow()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Arbor.i("saveNewPhoto() failed: $e")
            null
        }
    }
    //endregion


    //region Items
    fun onListItemClick(item: EasyListsListItem) {
        viewModelScope.launch {
            updateListItemUseCase(
                listItem = item.copy(
                    crossedOff = !item.crossedOff,
                    crossedOffTimestamp = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }
    //endregion


    //region Deletes the item currently open in the edit bottom sheet, then closes the sheet.
    fun deleteListItem() {
        val form = editor ?: return
        if (form.mode == AddEditMode.Add) return
        val id = form.itemId
        val name = form.original?.name.orEmpty()
        editor = null

        viewModelScope.launch {
            deleteListItemUseCase(listOf(id)).onSuccess {
                _events.send(ListItemsEvent.ListItemDeleted(itemId = id, itemName = name))
            }
        }
    }
    //endregion


    //region Delete all crossed-off items
    fun deleteAllCrossedOffItems() {
        val ids = successState?.items.orEmpty()
            .filter { it.crossedOff }
            .mapNotNull { it.listItemId }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            deleteListItemUseCase(ids).onSuccess {
                _events.send(ListItemsEvent.CrossedOffItemsDeleted(ids))
            }
        }
    }
    //endregion


    //region undo crossed-off delete
    fun onUndoDeleteCrossedOffItems(itemIds: List<String>) {
        viewModelScope.launch { undeleteListItemsUseCase(itemIds) }
    }
    //endregion


    //region undo item delete
    fun onUndoDeleteListItem(itemId: String) {
        viewModelScope.launch { undeleteListItemsUseCase(listOf(itemId)) }
    }
    //endregion


    //region restoreCrossedOffItems()
    fun restoreAllCrossedOffItems() {
        val currentListId = listId.value ?: return
        viewModelScope.launch { restoreCrossedOffListItemsUseCase(currentListId) }
    }
    //endregion


    //region Dialogs, tag pills, refresh
    fun setShowDeleteItemDialog(show: Boolean) {
        interaction = interaction.copy(showDeleteItemDialog = show)
    }

    fun setShowDeleteCrossedOffDialog(show: Boolean) {
        interaction = interaction.copy(showDeleteCrossedOffDialog = show)
    }

    fun onToggleTagPills() {
        interaction = interaction.copy(expandTagPills = !interaction.expandTagPills)
    }

    fun onRefresh() {
        interaction = interaction.copy(isRefreshing = true)
        refreshTrigger.update { it + 1 }
        viewModelScope.launch {
            delay(500.milliseconds) // keeps the indicator from flickering
            interaction = interaction.copy(isRefreshing = false)
        }
    }
    //endregion
}