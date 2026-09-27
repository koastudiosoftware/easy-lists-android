package com.easylists.presentation.ui.listdetails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.common.KEY
import com.easylists.domain.common.TYPE
import com.easylists.domain.common.VALUE
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.domain.use_cases.SaveListItemPhotoUseCase
import com.easylists.domain.use_cases.AddCategoryUseCase
import com.easylists.domain.use_cases.AddListItemFlowUseCase
import com.easylists.domain.use_cases.AddTagListItemUseCase
import com.easylists.domain.use_cases.GetAppSettingsUseCase
import com.easylists.domain.use_cases.GetCategoryFlowUseCase
import com.easylists.domain.use_cases.GetListItemFlowUseCase
import com.easylists.domain.use_cases.GetTagFlowUseCase
import com.easylists.domain.use_cases.GetTagListItemFlowUseCase
import com.easylists.domain.use_cases.RemoveListItemUseCase
import com.easylists.domain.use_cases.RemoveTagListItemUseCase
import com.easylists.domain.use_cases.UpdateListItemFlowUseCase
import com.easylists.presentation.BuildConfig
import com.easylists.presentation.common.AddEditMode
import com.easylists.presentation.common.AppSettingsKeys
import com.easylists.presentation.common.Capitalization
import com.easylists.presentation.common.GroupCrossedOffItems
import com.easylists.presentation.common.ImageBitmapLoader
import com.easylists.presentation.common.SortCrossedOffItems
import com.easylists.presentation.common.isNumeric
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getCategoryFlowUseCase: GetCategoryFlowUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getTagFlowUseCase: GetTagFlowUseCase,
    private val getTagListItemFlowUseCase: GetTagListItemFlowUseCase,
    private val addTagListItemUseCase: AddTagListItemUseCase,
    private val removeTagListItemUseCase: RemoveTagListItemUseCase,
    private val getListItemFlowUseCase: GetListItemFlowUseCase,
    private val addListItemUseCase: AddListItemFlowUseCase,
    private val updateListItemUseCase: UpdateListItemFlowUseCase,
    private val removeListItemUseCase: RemoveListItemUseCase,
    private val saveListItemPhotoUseCase: SaveListItemPhotoUseCase,
    private val bitmapLoader: ImageBitmapLoader,
    private val mapper: UiMapper,
//    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private var categoryListFlowJob: Job? = null
    private var listItemListFlowJob: Job? = null
    private var tagListFlowJob: Job? = null
    private var tagListItemListFlowJob: Job? = null

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

            val enableCamera =
                result.find { it[KEY] == AppSettingsKeys.EnableCamera.key }?.get(VALUE)

            val enableTags =
                result.find { it[KEY] == AppSettingsKeys.EnableTags.key }?.get(VALUE)

            val groupCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.GroupCrossedOffItems.key }?.get(VALUE)

            val sortCrossedOffItems =
                result.find { it[KEY] == AppSettingsKeys.SortCrossedOffItems.key }?.get(VALUE)

            state = state.copy(
                capitalization = Capitalization.from(
                    capitalization ?: Capitalization.NoCapitalization.toString()
                ) ?: Capitalization.NoCapitalization,

                enableCamera = enableCamera != "false",

                enableTags = enableTags != "false",

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


    //region saveListItem() :: save a list item to the database
    @OptIn(ExperimentalUuidApi::class)
    fun saveListItem() {
        viewModelScope.launch {
            saveListItemPhoto()
            Arbor.i("saveListItem() state.itemPhotoUri: ${state.itemPhotoUri}")

            var category: EasyListsCategory

            removeTagListItem()
            addTagListItem()

            var categoryUid = state.categoryList.find { it.name == state.categoryText }?.uid
            if (categoryUid == null && state.categoryText.isNotEmpty()) {
                categoryUid = Uuid.random().toString()
                category = EasyListsCategory(
                    uid = categoryUid,
                    name = state.categoryText,
                )
                addCategoryUseCase(category)
            } else if (categoryUid == null) {
                // special case where the user selected the empty category and
                // wants to remove the category from the list item
                categoryUid = null
            }

            delay(100L.milliseconds)     // allow a short time for the category to be added to the database
            val listItem = EasyListsListItem(
                uid = state.itemUid,
                listUid = state.listUid,
                name = state.itemName,
                categoryUid = categoryUid,
                notes = state.itemNotes.ifEmpty { null },
                quantity = if (state.itemQuantity.isEmpty()) null else state.itemQuantity.toInt(),
                photoUri = state.photoUri,
                photoScale = state.photoScale,
                photoOffsetX = state.photoOffset.x.toDouble(),
                photoOffsetY = state.photoOffset.y.toDouble(),
            )

            Arbor.i("listItem: $listItem")

            if (state.addEditMode == AddEditMode.Add) {
                addListItemUseCase(listItem = listItem)
            } else {
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


    //region addTagListItem()
    @OptIn(ExperimentalUuidApi::class)
    fun addTagListItem() {
        val selectedTags = state.easyListsTagList.filter { it.isSelected }
        val tagsToAdd = selectedTags.filter { tag ->
            state.tagListItemList.none { tagListItem ->
                tagListItem.listItemUid == state.itemUid && tagListItem.tagUid == tag.uid
            }
        }

        viewModelScope.launch {
            addTagListItemUseCase(tagListItem = tagsToAdd.map {
                TagListItem(
                    uid = Uuid.random().toString(),
                    listItemUid = state.itemUid,
                    tagUid = it.uid.toString()
                )
            })
        }
    }
    //endregion


    //region removeTagListItem()
    fun removeTagListItem() {
        val deselectedTags = state.easyListsTagList.filter { !it.isSelected }
        val tagsToRemove = deselectedTags.filter { tag ->
            state.tagListItemList.any { tagListItem ->
                tagListItem.listItemUid == state.itemUid && tagListItem.tagUid == tag.uid
            }
        }

        viewModelScope.launch {
            removeTagListItemUseCase(
                listItemUid = state.itemUid,
                tagUidList = tagsToRemove.map { it.uid.toString() }
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


    //region itemPhotoScale()
    fun itemPhotoScale(): Double {
        return state.photoScale
    }
    //endregion


    //region listItemIconButtonEnabled()
    fun listItemIconButtonEnabled(): Boolean {
        if (state.itemName.isEmpty()) return false

        if (state.addEditMode == AddEditMode.Edit) return true

        // don't allow duplicate item name
        if (state.listItemList.any { it.name.equals(state.itemName, ignoreCase = true) }) return false

        return true
    }
    //endregion


    //region onItemBottomSheetDismiss()
    @OptIn(ExperimentalUuidApi::class)
    fun onItemBottomSheetDismiss() {
        state = state.copy(
            addEditMode = AddEditMode.Add,
            itemUid = Uuid.random().toString(),
            itemName = "",
            itemNotes = "",
            itemQuantity = "",
            itemNameInvalid = false,
            itemNameInvalidMessage = "",
            itemPhotoUri = null,
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
            it.name.equals(name, ignoreCase = true)
        }).let {
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
        result.onSuccess { it ->
            Arbor.i("handleGetListItemState() it: $it")
            var groupedItemList: Map<Pair<Boolean?, String?>, List<EasyListsListItem>>? = null

            if (state.categoryList.isNotEmpty()) {
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
                nextDataFetchStage = "tag",
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
                // TODO this is where the sorting order should be applied
                categoryList = it?.map { item -> item } ?: emptyList(),
                nextDataFetchStage = "list item",
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


    //region initTagList() :: initialize list of tags from the database
    fun initTagList() {
        cancelTagFlowCollection()

        tagListFlowJob = getTagFlowUseCase()
            .onEach {
                handleGetTagListState(Resultat.success(it))
            }.catch {
                handleGetTagListState(Resultat.failure(it))

                // After this catch the flow is interrupted, and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelTagFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetTagListState(result: Resultat<List<EasyListsTag>?>) {
        result.onSuccess {
            state = state.copy(
//                isPullToRefreshing = false,
                easyListsTagList = it?.map { item -> item } ?: emptyList(),
                nextDataFetchStage = "tag list item",
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


    private fun cancelTagFlowCollection() {
        tagListFlowJob?.cancel()
        tagListFlowJob = null
    }
    //endregion


    //region initTagListItemList() :: initialize list of tags from the database
    fun initTagListItemList() {
        cancelTagListItemFlowCollection()

        tagListItemListFlowJob = getTagListItemFlowUseCase()
            .onEach {
                delay(500L.milliseconds) // workaround to eliminate sticky pull to refresh indicator
                handleGetTagListItemListState(Resultat.success(it))
            }.catch {
                handleGetTagListItemListState(Resultat.failure(it))

                // After this catch the flow is interrupted and it must be collected
                // again to obtain new data. The handleRefresh() method handles this situation.
                cancelTagListItemFlowCollection()
            }.launchIn(viewModelScope)
    }


    private fun handleGetTagListItemListState(result: Resultat<List<TagListItem>?>) {
        result.onSuccess {
            state = state.copy(
                isPullToRefreshing = false,
                tagListItemList = it?.map { item -> item } ?: emptyList(),
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


    private fun cancelTagListItemFlowCollection() {
        tagListItemListFlowJob?.cancel()
        tagListItemListFlowJob = null
    }
    //endregion


    //region onPullToRefresh()
    fun onPullToRefresh(): () -> Unit = {
        state = state.copy(
            isPullToRefreshing = true,
            nextDataFetchStage = "category",
        )
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

        Arbor.i("onListItemInfoClick() item: $item")
        state = state.copy(
            addEditMode = addEditMode,
            categoryText = category?.name ?: "",
            itemName = item.name,
            itemNotes = item.notes ?: "",
            itemQuantity = item.quantity?.toString() ?: "",
            itemUid = item.uid.toString(),
            itemPhotoUri = item.photoUri,
            itemPhotoScale = item.photoScale,
            itemPhotoOffset = Offset(item.photoOffsetX.toFloat(), item.photoOffsetY.toFloat()),
            selectedCategoryIndex = index,
            showListItemBottomSheet = true
        )

        selectedTags()
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


    //region onSelectedIdsChange
    fun onSelectedIdsChange(ids: List<Int>) {
        state = state.copy(selectedTagIds = ids)
    }
    //endregion


    //region onTagClick()
    fun onTagClick(easyListsTag: EasyListsTag) {
        // create a copy of the list
        val tagList = ArrayList( state.easyListsTagList.map { it.copy() })

        tagList.find {
            it.uid == easyListsTag.uid
        }?.isSelected = !easyListsTag.isSelected

        state = state.copy(easyListsTagList = tagList)
    }
    //endregion


    //region selectedTags()
    // finds the set of tags that are associated with the selected list item
    fun selectedTags() {
        val tagList = state.easyListsTagList
        val selectedTags = tagList.filter { tag ->
            state.tagListItemList.filter { tagListItem ->
                tagListItem.listItemUid == state.itemUid
            }.any { it.tagUid == tag.uid }
        }

        tagList.forEach { tag ->
            tag.isSelected = selectedTags.any { it.uid == tag.uid }
        }

        state = state.copy(easyListsTagList = tagList)
    }
    //endregion


    //region onCameraImageSaved()
    fun onCameraImageSaved(context: Context) {
        // We get here when the user saves the image they took with the camera
        Arbor.i("Camera image saved")
        Arbor.i("state.tempCameraFileUrl: ${state.tempCameraFileUrl}")
        state = state.copy(
            itemPhotoUri = state.tempCameraFileUrl.toString(),
            tempCameraFileUrl = null
        )

    }
    //endregion


    //region onCameraPermissionDenied()
    fun onCameraPermissionDenied() {
        Arbor.i("Camera permission denied")
        // TODO Display a message in the app to explain that permissions were denied
        // TODO Save this in the app settings so we can show the message every time?
        // TODO And then we can disable (but not hide) the camera icon?
    }
    //endregion


    //region onCameraImageSavingCanceled()
    fun onCameraImageSavingCanceled() {
        // We get here when the camera is active and a photo was taken but the user
        // decided not to save the image they took, no need to do anything here
        Arbor.i("Camera image saving canceled")
        state = state.copy(tempCameraFileUrl = null)
    }
    //endregion


    //region onCameraPermissionGranted()
    @SuppressLint("SimpleDateFormat")
    fun onCameraPermissionGranted(context: Context) {
        Arbor.i("Camera permission granted")

        // Create an empty image file in the app's cache directory before the camera
        // opens up to allow the user to take a photo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = File.createTempFile(
            "camera_" + timeStamp + "_",
            ".jpg",
            context.cacheDir
        )

        // Create sandboxed url for this temp file - needed for the camera API
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.LIBRARY_PACKAGE_NAME}.provider",
            file
        )
        state = state.copy(tempCameraFileUrl = uri)
    }
    //endregion


    //region onFinishPickingImages()
    fun onFinishPickingImages(context: Context, uri: Uri?) {
        Arbor.i("FinishPickingImages() uri: $uri")
        state = state.copy(itemPhotoUri = uri.toString())
    }
    //endregion


    //region expandTagPills()
    fun expandTagPills() {
        state = state.copy(expandTagPills = !state.expandTagPills)
    }
    //endregion


    //region listItemTags()
    fun listItemTags(item: EasyListsListItem): List<EasyListsTag> {
        val tagListItems = state.tagListItemList.filter {
            it.listItemUid == item.uid
        }

        val tags = state.easyListsTagList.filter { tag ->
            tagListItems.any { it.tagUid == tag.uid }
        }

        return tags
    }
    //endregion


    //region updatePhotoTransfor()
    // save the new scale and offset values to state
    fun updatePhotoTransform(scale: Float, offsetX: Float, offsetY: Float) {
        state = state.copy(
            photoScale = scale.toDouble(),
            photoOffset = Offset(offsetX, offsetY)
        )
    }
    //endregion


    //region onPhotoOffsetChange()
    fun onPhotoOffsetChange(offset: Offset) {
        state = state.copy(photoOffset = offset)
    }
    //endregion


    //region onPhotoScaleChange()
    fun onPhotoScaleChange(scale: Double) {
        state = state.copy(photoScale = scale)
    }
    //endregion


    //region saveListItemPhoto()
    // this function saves the temporary photo or loaded image file to app-private storage
    // the returned string value is the full URI to the saved file which should be saved
    // to the database along with the rest of the photo-related data (scale, offset)
    suspend fun saveListItemPhoto() {
        val imageUri = state.itemPhotoUri ?: return
        val bitmap = bitmapLoader.load(imageUri)
            ?: // _saveState.value = SaveState.Error("Could not load image")
            return

        val bytes = ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
        val filename = "img_${System.currentTimeMillis()}.png"
        val result = saveListItemPhotoUseCase(bytes, filename)
        state = state.copy(photoUri = result.getOrThrow());

        // this code appears to be simply saving the state of the save so the UI
        // can react to whether the save was successful or not
        // TODO circle back to this later
//            _saveState.value = result.fold(
//                onSuccess = { SaveState.Success(it) },
//                onFailure = { SaveState.Error(it.message) }
//            )
    }
    //endregion

}
