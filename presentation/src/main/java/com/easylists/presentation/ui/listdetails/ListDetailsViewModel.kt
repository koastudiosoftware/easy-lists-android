package com.easylists.presentation.ui.listdetails

import android.graphics.Bitmap
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
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.AddListItemFlowUseCase
import com.easylists.domain.use_cases.AddTagListItemUseCase
import com.easylists.domain.use_cases.DeleteListItemsUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.GetTagFlowUseCase
import com.easylists.domain.use_cases.GetTagListItemFlowUseCase
import com.easylists.domain.use_cases.ObserveAppSettingsUseCase
import com.easylists.domain.use_cases.RemoveTagListItemUseCase
import com.easylists.domain.use_cases.SaveListItemPhotoUseCase
import com.easylists.domain.use_cases.UpdateListItemFlowUseCase
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.ImageBitmapLoader
import com.easylists.presentation.mappers.UiMapper
import com.easylists.presentation.models.ItemEditorState
import com.easylists.presentation.models.ListDetailsInteractionState
import com.easylists.presentation.models.ListDetailsUiState
import com.easylists.presentation.models.PhotoDraft
import com.easylists.presentation.models.PhotoTransform
import com.easylists.presentation.models.validate
import com.toxicbakery.logging.Arbor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    observeAppSettings: ObserveAppSettingsUseCase,
    getListItemFlowUseCase: GetListItemFlowUseCase,
    getCategoryFlowUseCase: GetCategoryFlowUseCase,
    getTagFlowUseCase: GetTagFlowUseCase,
    getTagListItemFlowUseCase: GetTagListItemFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val addTagListItemUseCase: AddTagListItemUseCase,
    private val removeTagListItemUseCase: RemoveTagListItemUseCase,
    private val addListItemUseCase: AddListItemFlowUseCase,
    private val updateListItemUseCase: UpdateListItemFlowUseCase,
    private val deleteListItemUseCase: DeleteListItemsUseCase,
    private val saveListItemPhotoUseCase: SaveListItemPhotoUseCase,
    private val bitmapLoader: ImageBitmapLoader,
    private val session: SessionRepository,
    private val mapper: UiMapper,
) : ViewModel() {

    private val listId = MutableStateFlow<String?>(null)
    private val refreshTrigger = MutableStateFlow(0)

    var listName by mutableStateOf("")
        private set

    // Everything the list shows comes from one combined flow. A change to the items,
    // categories, tags, tag links, or settings rebuilds the rows, so nothing is copied
    // into state and no fetch ordering or delay is needed.
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListDetailsUiState> =
        combine(listId.filterNotNull(), refreshTrigger) { id, _ -> id }
            .flatMapLatest { id ->
                combine(
                    getListItemFlowUseCase(listId = id),
                    getCategoryFlowUseCase(),
                    getTagFlowUseCase(),
                    getTagListItemFlowUseCase(),
                    observeAppSettings(),
                ) { items, categories, tags, tagLinks, settings ->
                    buildListDetailsState(
                        items = items.orEmpty(),
                        categories = categories.orEmpty(),
                        tags = tags.orEmpty(),
                        tagLinks = tagLinks.orEmpty(),
                        settings = settings,
                    )
                }.catch { emit(ListDetailsUiState.Error(message = mapper.mapErrorToUiMessage(it))) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListDetailsUiState.Loading)

    // null = bottom sheet closed. Compose state so the TextFields read and write synchronously.
    var editor by mutableStateOf<ItemEditorState?>(null)
        private set

    var interaction by mutableStateOf(ListDetailsInteractionState())
        private set

    private val successState: ListDetailsUiState.Success?
        get() = uiState.value as? ListDetailsUiState.Success


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

    fun onAddItemClick() {
        editor = ItemEditorState(itemId = Uuid.random().toString())
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
                    scale = item.photoScale.toFloat().takeIf { it > 0f } ?: 1f,
                    offsetX = item.photoOffsetX.toFloat(),
                    offsetY = item.photoOffsetY.toFloat(),
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
    fun saveListItem(onSaved: () -> Unit) {
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
                photoScale = (photo?.current?.scale ?: 1f).toDouble(),
                photoOffsetX = (photo?.current?.offsetX ?: 0f).toDouble(),
                photoOffsetY = (photo?.current?.offsetY ?: 0f).toDouble(),
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

    private suspend fun createCategory(name: String): String? {
        // TODO surface an error if the userId can't be fetched
        val userId = session.getUserId().ifEmpty { return null }
        val id = Uuid.random().toString()
        addCategoryUseCase(EasyListsCategory(categoryId = id, ownerId = userId, name = name))
        return id
    }

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
                        tagListItemId = Uuid.random().toString(),
                        listItemId = itemId,
                        tagId = it,
                    )
                }
            )
        }
    }

    // copies the chosen image to app-private storage and returns its path
    private suspend fun saveNewPhoto(sourceUri: String): String? {
        // TODO surface an error to the user when the image can't be loaded or saved
        val bitmap = bitmapLoader.load(sourceUri) ?: return null
        val bytes = withContext(Dispatchers.Default) {
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        }
        val filename = "img_${System.currentTimeMillis()}.png"
        return try {
            saveListItemPhotoUseCase(bytes, filename).getOrThrow()
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
                    crossedOff = item.crossedOff != true,
                    crossedOffTimestamp = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }

    fun showContextIcons(item: EasyListsListItem) {
        interaction = interaction.copy(
            selectedItemId = if (interaction.selectedItemId == null) item.listItemId else null,
        )
    }

    fun deleteListItem() {
        val id = interaction.selectedItemId ?: return
        viewModelScope.launch {
            deleteListItemUseCase(id)
            interaction = interaction.copy(selectedItemId = null)
        }
    }

    fun deleteAllCrossedOffItems() {
        val crossedOff = successState?.items.orEmpty().filter { it.crossedOff == true }
        viewModelScope.launch {
            crossedOff.forEach { updateListItemUseCase(listItem = it.copy(isDeleted = true)) }
        }
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
