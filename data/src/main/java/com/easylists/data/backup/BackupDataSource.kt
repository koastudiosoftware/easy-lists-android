package com.easylists.data.backup

import androidx.room.withTransaction
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.ListOrderEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.Capitalization
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.ListSortMode
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.common.ViewMode
import com.easylists.domain.repositories.SessionRepository
import com.easylists.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Turns the user's data into a BackupFile and back. It knows nothing about zips, folders or the file
// picker: the caller handles those and passes in a way to resolve photo file names.
class BackupDataSource @Inject constructor(
    private val database: EasyListsDatabase,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
) {

    //region Export

    // Everything is read inside one transaction so the backup is a consistent snapshot.
    suspend fun export(sections: Set<BackupSection>): BackupFile {
        val userId = sessionRepository.getUserId()
        val settings = if (BackupSection.Settings in sections) {
            settingsRepository.settings.first().toBackup()
        } else {
            null
        }
        val dao = database.backupDao()

        return database.withTransaction {
            val withLists = BackupSection.Lists in sections
            val withTags = BackupSection.Tags in sections

            val lists = if (withLists) dao.getOwnedLists(userId) else emptyList()
            val items = if (withLists) dao.getOwnedListItems(userId) else emptyList()
            val order = if (withLists) dao.getListOrder(userId) else emptyList()
            val categories =
                if (BackupSection.Categories in sections) dao.getOwnedCategories(userId) else emptyList()
            val tags = if (withTags) dao.getOwnedTags(userId) else emptyList()
            val links = if (withTags) dao.getOwnedTagLinks(userId) else emptyList()

            BackupFile(
                formatVersion = BackupFile.CURRENT_FORMAT_VERSION,
                createdTimestamp = System.currentTimeMillis(),
                userId = userId,
                sections = BackupSection.entries.filter { it in sections }.map { it.id },
                settings = settings,
                categories = categories.map { it.toBackup() },
                tags = tags.map { it.toBackup() },
                lists = lists.map { it.toBackup() },
                listItems = items.map { it.toBackup() },
                tagLinks = links.map { it.toBackup() },
                listOrder = order.map { it.toBackup() },
            )
        }
    }

    //endregion

    //region Restore

    /**
     * Replaces what the user owns, in the given sections, with the contents of [file].
     *
     * [photoPathFor] receives a photo file name from the backup and returns the value to store in
     * photo_uri, or null when that photo isn't available. Items with an unavailable photo are restored
     * without a photo. The caller must make sure the name can't point outside the photo folder.
     *
     * [sections] defaults to everything in the file. Sections that aren't listed are left untouched.
     *
     * Throws [BackupFormatException] before changing anything if the file can't be restored.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun restore(
        file: BackupFile,
        photoPathFor: (fileName: String) -> String?,
        sections: Set<BackupSection> = file.sectionSet(),
    ): RestoreSummary {
        validate(file)

        val toRestore = sections intersect file.sectionSet()
        val currentUserId = sessionRepository.getUserId()
        // A backup from a different user ID (the usual reinstall case) is restored as a fresh copy
        // with new IDs, so it can't collide with the old copy's records on the server.
        val regenerateIds = file.userId != currentUserId
        val now = System.currentTimeMillis()
        val dao = database.backupDao()

        // old ID -> new ID, filled only while regenerating and only for sections that are restored
        val categoryIds = HashMap<String, String>()
        val tagIds = HashMap<String, String>()
        val listIds = HashMap<String, String>()
        val itemIds = HashMap<String, String>()
        val linkIds = HashMap<String, String>()

        fun fresh(map: HashMap<String, String>, oldId: String): String =
            if (regenerateIds) map.getOrPut(oldId) { Uuid.generateV7().toString() } else oldId

        fun ref(map: Map<String, String>, oldId: String): String = map[oldId] ?: oldId

        val summary = database.withTransaction {

            // 1. Soft-delete what the user currently owns in the sections being restored.
            //    Soft deletes (not hard deletes) are what let the user's other devices learn about it.
            if (BackupSection.Categories in toRestore) {
                dao.softDeleteOwnedCategories(currentUserId, now)
            }
            if (BackupSection.Tags in toRestore) {
                dao.softDeleteOwnedTagLinks(currentUserId, now)
                dao.softDeleteOwnedTags(currentUserId, now)
            }
            if (BackupSection.Lists in toRestore) {
                dao.softDeleteOwnedListItems(currentUserId, now)
                dao.softDeleteOwnedLists(currentUserId, now)
            }

            // 2. Upsert the backup's rows in foreign-key order. Every restored row is marked dirty
            //    with a fresh modified timestamp so it wins over older copies when sync runs.
            var categoryCount = 0
            if (BackupSection.Categories in toRestore) {
                val rows = file.categories.orEmpty().mapNotNull { c ->
                    val id = c.id ?: return@mapNotNull null
                    val name = c.name ?: return@mapNotNull null
                    CategoryEntity(
                        categoryId = fresh(categoryIds, id),
                        ownerId = currentUserId,
                        name = name,
                        isDirty = true,
                        isDeleted = false,
                        createdTimestamp = c.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                    )
                }
                dao.upsertCategories(rows)
                categoryCount = rows.size
            }

            var tagCount = 0
            if (BackupSection.Tags in toRestore) {
                val rows = file.tags.orEmpty().mapNotNull { t ->
                    val id = t.id ?: return@mapNotNull null
                    val name = t.name ?: return@mapNotNull null
                    TagEntity(
                        tagId = fresh(tagIds, id),
                        ownerId = currentUserId,
                        name = name,
                        color = t.color,
                        isDirty = true,
                        isDeleted = false,
                        createdTimestamp = t.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                    )
                }
                dao.upsertTags(rows)
                tagCount = rows.size
            }

            var listCount = 0
            var itemCount = 0
            if (BackupSection.Lists in toRestore) {
                val listRows = file.lists.orEmpty().mapNotNull { l ->
                    val id = l.id ?: return@mapNotNull null
                    val name = l.name ?: return@mapNotNull null
                    ListEntity(
                        listId = fresh(listIds, id),
                        ownerId = currentUserId,
                        name = name,
                        notes = l.notes,
                        isDirty = true,
                        isDeleted = false,
                        createdTimestamp = l.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                    )
                }
                dao.upsertLists(listRows)
                listCount = listRows.size
                val restoredListIds = listRows.map { it.listId }.toHashSet()

                // A category that isn't live after the categories step is cleared instead of letting the
                // foreign key fail the whole restore.
                val liveCategoryIds = dao.getLiveCategoryIds().toHashSet()
                val itemRows = file.listItems.orEmpty().mapNotNull { i ->
                    val id = i.id ?: return@mapNotNull null
                    val name = i.name ?: return@mapNotNull null
                    val listId = ref(listIds, i.listId ?: return@mapNotNull null)
                    if (listId !in restoredListIds) return@mapNotNull null
                    val photoPath = i.photoFileName?.let(photoPathFor)
                    ListItemEntity(
                        listItemId = fresh(itemIds, id),
                        listId = listId,
                        categoryId = i.categoryId
                            ?.let { ref(categoryIds, it) }
                            ?.takeIf { it in liveCategoryIds },
                        name = name,
                        notes = i.notes,
                        quantity = i.quantity,
                        crossedOff = i.crossedOff ?: false,
                        crossedOffTimestamp = i.crossedOffTimestamp,
                        photoUri = photoPath,
                        photoOffsetX = if (photoPath != null) i.photoOffsetX else null,
                        photoOffsetY = if (photoPath != null) i.photoOffsetY else null,
                        photoScale = if (photoPath != null) i.photoScale else null,
                        isDirty = true,
                        isDeleted = false,
                        createdTimestamp = i.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                    )
                }
                dao.upsertListItems(itemRows)
                itemCount = itemRows.size

                val orderRows = file.listOrder.orEmpty().mapNotNull { o ->
                    val listId = ref(listIds, o.listId ?: return@mapNotNull null)
                    if (listId !in restoredListIds) return@mapNotNull null
                    ListOrderEntity(
                        userId = currentUserId,
                        listId = listId,
                        sortIndex = o.sortIndex ?: return@mapNotNull null,
                        isDirty = true,
                        createdTimestamp = o.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                    )
                }
                dao.upsertListOrder(orderRows)
            }

            // Links go last. Both ends must be live at this point, which covers restoring tags without
            // lists (or the other way round) onto a database that doesn't have the other half.
            var linkCount = 0
            if (BackupSection.Tags in toRestore) {
                val liveTagIds = dao.getLiveTagIds().toHashSet()
                val liveItemIds = dao.getLiveListItemIds().toHashSet()
                val linkRows = file.tagLinks.orEmpty().mapNotNull { link ->
                    val id = link.id ?: return@mapNotNull null
                    val tagId = ref(tagIds, link.tagId ?: return@mapNotNull null)
                    val itemId = ref(itemIds, link.listItemId ?: return@mapNotNull null)
                    if (tagId !in liveTagIds || itemId !in liveItemIds) return@mapNotNull null
                    TagListItemEntity(
                        tagListItemId = fresh(linkIds, id),
                        tagId = tagId,
                        listItemId = itemId,
                        createdTimestamp = link.createdTimestamp ?: now,
                        modifiedTimestamp = now,
                        isDirty = true,
                        isDeleted = false,
                    )
                }
                dao.upsertTagLinks(linkRows)
                linkCount = linkRows.size
            }

            RestoreSummary(
                lists = listCount,
                listItems = itemCount,
                categories = categoryCount,
                tags = tagCount,
                tagLinks = linkCount,
                settingsRestored = false,
                idsRegenerated = regenerateIds,
            )
        }

        // Settings are written only after the data restore succeeded. DataStore isn't part of the
        // Room transaction, so a failure above leaves the settings untouched.
        val backedUpSettings = file.settings
        if (BackupSection.Settings in toRestore && backedUpSettings != null) {
            settingsRepository.update { current -> current.withBackup(backedUpSettings) }
            return summary.copy(settingsRestored = true)
        }
        return summary
    }

    private fun validate(file: BackupFile) {
        val version = file.formatVersion
            ?: throw BackupFormatException("This isn't an Easy Lists backup file.")
        if (version < 1) {
            throw BackupFormatException("This isn't an Easy Lists backup file.")
        }
        if (version > BackupFile.CURRENT_FORMAT_VERSION) {
            throw BackupFormatException("This backup was made by a newer version of the app. Update the app to restore it.")
        }
        if (file.userId.isNullOrBlank()) {
            throw BackupFormatException("This backup file is incomplete.")
        }
        if (file.sectionSet().isEmpty()) {
            throw BackupFormatException("This backup doesn't contain anything to restore.")
        }
    }

    //endregion

    //region Mapping

    private fun AppSettings.toBackup() = BackupSettings(
        capitalization = capitalization.id,
        enableCamera = enableCamera,
        enablePhotos = enablePhotos,
        enableTags = enableTags,
        groupCrossedOffItems = groupCrossedOffItems.id,
        sortCrossedOffItems = sortCrossedOffItems.id,
        viewMode = viewMode.id,
        listSortMode = listSortMode.id,
    )

    // A value that is missing or not recognized keeps the user's current setting.
    private fun AppSettings.withBackup(backup: BackupSettings) = copy(
        capitalization = Capitalization.from(backup.capitalization) ?: capitalization,
        enableCamera = backup.enableCamera ?: enableCamera,
        enablePhotos = backup.enablePhotos ?: enablePhotos,
        enableTags = backup.enableTags ?: enableTags,
        groupCrossedOffItems = GroupCrossedOffItems.from(backup.groupCrossedOffItems)
            ?: groupCrossedOffItems,
        sortCrossedOffItems = SortCrossedOffItems.from(backup.sortCrossedOffItems)
            ?: sortCrossedOffItems,
        viewMode = ViewMode.from(backup.viewMode) ?: viewMode,
        listSortMode = ListSortMode.from(backup.listSortMode) ?: listSortMode,
    )

    private fun CategoryEntity.toBackup() = BackupCategory(
        id = categoryId,
        name = name,
        createdTimestamp = createdTimestamp,
    )

    private fun TagEntity.toBackup() = BackupTag(
        id = tagId,
        name = name,
        color = color,
        createdTimestamp = createdTimestamp,
    )

    private fun ListEntity.toBackup() = BackupList(
        id = listId,
        name = name,
        notes = notes,
        createdTimestamp = createdTimestamp,
    )

    // photo_uri may hold a full path today and a bare file name later; either way only the name is stored.
    private fun ListItemEntity.toBackup() = BackupListItem(
        id = listItemId,
        listId = listId,
        categoryId = categoryId,
        name = name,
        notes = notes,
        quantity = quantity,
        crossedOff = crossedOff,
        crossedOffTimestamp = crossedOffTimestamp,
        photoFileName = photoUri?.let { File(it).name },
        photoOffsetX = photoOffsetX,
        photoOffsetY = photoOffsetY,
        photoScale = photoScale,
        createdTimestamp = createdTimestamp,
    )

    private fun TagListItemEntity.toBackup() = BackupTagLink(
        id = tagListItemId,
        tagId = tagId,
        listItemId = listItemId,
        createdTimestamp = createdTimestamp,
    )

    private fun ListOrderEntity.toBackup() = BackupListOrder(
        listId = listId,
        sortIndex = sortIndex,
        createdTimestamp = createdTimestamp,
    )

    //endregion

}