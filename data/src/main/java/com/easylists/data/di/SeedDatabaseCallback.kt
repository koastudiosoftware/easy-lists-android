package com.easylists.data.di

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.models.CategoryEntity
import com.easylists.data.db.room.models.ListEntity
import com.easylists.data.db.room.models.ListItemEntity
import com.easylists.data.db.room.models.TagEntity
import com.easylists.data.db.room.models.TagListItemEntity
import com.easylists.data.db.room.models.UserEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// --- JSON model: semantic content only. IDs, owner, timestamps and flags are added in Kotlin. ---

data class SeedData(
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val lists: List<SeedList> = emptyList(),
)

data class SeedList(
    val name: String,
    val notes: String? = null,
    val items: List<SeedItem> = emptyList(),
)

data class SeedItem(
    val name: String,
    val notes: String? = null,
    val category: String? = null,   // must match a name in "categories"
    val tags: List<String>? = null, // each must match a name in "tags"
    val crossedOff: Boolean? = null,
)

// --- Callback ---

/**
 * Seeds the database the first time it is created.
 *
 * [database] is a lazy reference (see the Hilt wiring example at the bottom of this file): inside onCreate the database
 * isn't usable yet, so the actual inserts run in a coroutine that goes through the DAOs
 * once creation has finished.
 */
class SeedDatabaseCallback(
    private val context: Context,
    private val database: () -> EasyListsDatabase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            try {
                seed()
            } catch (e: Exception) {
                Log.e(TAG, "Seeding failed", e)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun seed() {
        val seedData = context.assets.open(SEED_ASSET).bufferedReader().use {
            Gson().fromJson(it, SeedData::class.java)
        }
        val db = database()
        val now = System.currentTimeMillis()

        // One transaction: either everything is seeded or nothing is.
        db.withTransaction {
            // Seeded rows need an owner, so create the local-only user first.
            val localUser = UserEntity(
                userId = newId(),
                email = null,
                displayName = null,
                profilePictureUrl = null,
                syncEnabled = false,
            )
            db.userDao().upsert(localUser)

            val ownerId = localUser.userId

            // The insertAll methods are assumed; add them to the DAOs if you don't have them.
            // Name -> generated ID, so list items can refer to categories and tags by name.
            val categoryIds = seedData.categories.associateWith { newId() }
            val tagIds = seedData.tags.associateWith { newId() }

            db.categoryDao().insertAll(
                seedData.categories.map {
                    CategoryEntity(
                        categoryId = categoryIds.getValue(it),
                        ownerId = ownerId,
                        name = it,
                        isDirty = true,
                        createdTimestamp = now,
                        modifiedTimestamp = now,
                    )
                }
            )

            db.tagDao().insertAll(
                seedData.tags.map {
                    TagEntity(
                        tagId = tagIds.getValue(it),
                        ownerId = ownerId,
                        name = it,
                        isDirty = true,
                        createdTimestamp = now,
                        modifiedTimestamp = now,
                    )
                }
            )

            seedData.lists.forEach { seedList ->
                val listId = newId()
                db.listDao().insert(
                    ListEntity(
                        listId = listId,
                        ownerId = ownerId,
                        name = seedList.name,
                        notes = seedList.notes,
                        isDirty = true,
                        createdTimestamp = now,
                        modifiedTimestamp = now,
                    )
                )
                val itemEntities = mutableListOf<ListItemEntity>()
                val tagLinks = mutableListOf<TagListItemEntity>()

                seedList.items.forEach { item ->
                    val listItemId = newId()
                    itemEntities += ListItemEntity(
                        listItemId = listItemId,
                        listId = listId,
                        categoryId = item.category?.let {
                            requireNotNull(categoryIds[it]) { "Seed item '${item.name}' has unknown category '$it'" }
                        },
                        crossedOff = item.crossedOff,
                        crossedOffTimestamp = if (item.crossedOff == true) now else null,
                        name = item.name,
                        notes = item.notes,
                        isDirty = true,
                        createdTimestamp = now,
                        modifiedTimestamp = now,
                    )
                    item.tags.orEmpty().forEach { tagName ->
                        tagLinks += TagListItemEntity(
                            tagId = requireNotNull(tagIds[tagName]) { "Seed item '${item.name}' has unknown tag '$tagName'" },
                            listItemId = listItemId,
                            createdTimestamp = now,
                            modifiedTimestamp = now,
                        )
                    }
                }

                // List items first: tag links have foreign keys to them.
                db.listItemDao().insertAll(itemEntities)
                db.tagListItemDao().insertAll(tagLinks)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun newId(): String = Uuid.generateV7().toString()

    private companion object {
        const val TAG = "SeedDatabaseCallback"
        const val SEED_ASSET = "seed/seed_data.json" // app/src/main/assets/seed/seed_data.json
    }
}