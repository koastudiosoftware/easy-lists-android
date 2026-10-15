package com.easylists.data.backup

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName

// The sections a backup can contain. The ids are what gets written into the backup file.
enum class BackupSection(val id: String) {
    Settings("settings"),
    Lists("lists"),             // lists, list items and the user's manual list order
    Categories("categories"),
    Tags("tags"),               // tags and the links between tags and list items
    Photos("photos");           // only records that the image files are in the zip; the data code never branches on it

    companion object {
        fun fromId(id: String?): BackupSection? = entries.firstOrNull { it.id == id }
    }
}

// Thrown when a backup file can't be read or can't be restored by this version of the app.
// The message is written so it can be shown to the user.
class BackupFormatException(message: String) : Exception(message)

// Every field is nullable on purpose: Gson ignores Kotlin default values, so a field missing from an
// older or damaged file arrives as null. The restore code decides what to do about each null.
// Every field has an explicit @SerializedName so the format survives renaming or minification.
data class BackupFile(
    @SerializedName("format_version") val formatVersion: Int? = null,
    @SerializedName("created_timestamp") val createdTimestamp: Long? = null,
    // The user who made the backup. Rows don't carry an owner: everything in the file belongs to this user.
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("sections") val sections: List<String>? = null,
    @SerializedName("settings") val settings: BackupSettings? = null,
    @SerializedName("categories") val categories: List<BackupCategory>? = null,
    @SerializedName("tags") val tags: List<BackupTag>? = null,
    @SerializedName("lists") val lists: List<BackupList>? = null,
    @SerializedName("list_items") val listItems: List<BackupListItem>? = null,
    @SerializedName("tag_links") val tagLinks: List<BackupTagLink>? = null,
    @SerializedName("list_order") val listOrder: List<BackupListOrder>? = null,
) {

    fun sectionSet(): Set<BackupSection> =
        sections.orEmpty().mapNotNull { BackupSection.fromId(it) }.toSet()

    // The image files the zip needs to hold for this backup
    fun photoFileNames(): Set<String> =
        listItems.orEmpty().mapNotNull { it.photoFileName }.toSet()

    // Counts for the "Restore this backup?" confirmation
    fun summary(): BackupSummary = BackupSummary(
        createdTimestamp = createdTimestamp,
        sections = sectionSet(),
        lists = lists.orEmpty().size,
        listItems = listItems.orEmpty().size,
        categories = categories.orEmpty().size,
        tags = tags.orEmpty().size,
        photos = photoFileNames().size,
    )

    companion object {
        const val CURRENT_FORMAT_VERSION = 1
    }
}

data class BackupSummary(
    val createdTimestamp: Long?,
    val sections: Set<BackupSection>,
    val lists: Int,
    val listItems: Int,
    val categories: Int,
    val tags: Int,
    val photos: Int,
)

data class RestoreSummary(
    val lists: Int,
    val listItems: Int,
    val categories: Int,
    val tags: Int,
    val tagLinks: Int,
    val settingsRestored: Boolean,
    // true when the backup came from a different user ID, so every restored row got a new ID
    val idsRegenerated: Boolean,
)

// Settings are stored as the same id strings the SettingsRepository keeps in DataStore.
data class BackupSettings(
    @SerializedName("capitalization")
    val capitalization: String? = null,

    @SerializedName("enable_camera")
    val enableCamera: Boolean? = null,

    @SerializedName("enable_photos")
    val enablePhotos: Boolean? = null,

    @SerializedName("enable_tags")
    val enableTags: Boolean? = null,

    @SerializedName("group_crossed_off_items")
    val groupCrossedOffItems: String? = null,

    @SerializedName("sort_crossed_off_items")
    val sortCrossedOffItems: String? = null,

    @SerializedName("view_mode")
    val viewMode: String? = null,

    @SerializedName("list_sort_mode")
    val listSortMode: String? = null,
)

data class BackupCategory(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

data class BackupTag(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

data class BackupList(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

data class BackupListItem(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("list_id")
    val listId: String? = null,

    @SerializedName("category_id")
    val categoryId: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("quantity")
    val quantity: Int? = null,

    @SerializedName("crossed_off")
    val crossedOff: Boolean? = null,

    @SerializedName("crossed_off_timestamp")
    val crossedOffTimestamp: Long? = null,

    // Just the file name, never a path. The photo itself lives in the zip.
    @SerializedName("photo_file_name")
    val photoFileName: String? = null,

    @SerializedName("photo_offset_x")
    val photoOffsetX: Double? = null,

    @SerializedName("photo_offset_y")
    val photoOffsetY: Double? = null,

    @SerializedName("photo_scale")
    val photoScale: Double? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

data class BackupTagLink(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("tag_id")
    val tagId: String? = null,

    @SerializedName("list_item_id")
    val listItemId: String? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

data class BackupListOrder(
    @SerializedName("list_id")
    val listId: String? = null,

    @SerializedName("sort_index")
    val sortIndex: Int? = null,

    @SerializedName("created_timestamp")
    val createdTimestamp: Long? = null,
)

object BackupJson {

    private val gson = Gson()

    fun encode(file: BackupFile): String = gson.toJson(file)

    fun decode(json: String): BackupFile {
        try {
            return gson.fromJson(json, BackupFile::class.java)
                ?: throw BackupFormatException("The backup file is empty.")
        } catch (e: JsonParseException) {
            throw BackupFormatException("The backup file is damaged or isn't an Easy Lists backup.")
        }
    }
}
