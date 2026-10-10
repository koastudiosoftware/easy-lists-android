package com.easylists.presentation.ui.listdetails

import com.easylists.domain.common.AppSettings
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.models.EasyListsCategory
import com.easylists.domain.models.EasyListsListItem
import com.easylists.domain.models.EasyListsTag
import com.easylists.domain.models.TagListItem
import com.easylists.presentation.models.ListDetailsUiState
import com.easylists.presentation.models.ListRow


//region buildListDetailsState
fun buildListDetailsState(
    items: List<EasyListsListItem>,
    categories: List<EasyListsCategory>,
    tags: List<EasyListsTag>,
    tagLinks: List<TagListItem>,
    settings: AppSettings,
): ListDetailsUiState {
    val liveItems = items.filterNot { it.isDeleted }
    val liveCategories = categories.filterNot { it.isDeleted }
    val liveTags = tags.filterNot { it.isDeleted }

    return ListDetailsUiState.Success(
        rows = buildListRows(
            items = liveItems,
            categories = liveCategories,
            tags = liveTags,
            tagLinks = tagLinks,
            group = settings.groupCrossedOffItems,
            sort = settings.sortCrossedOffItems,
        ),
        items = liveItems,
        categories = liveCategories,
        tags = liveTags,
        tagLinks = tagLinks,
        settings = settings,
    )
}
//endregion


//region buildListRows
fun buildListRows(
    items: List<EasyListsListItem>,
    categories: List<EasyListsCategory>,
    tags: List<EasyListsTag>,
    tagLinks: List<TagListItem>,
    group: GroupCrossedOffItems,
    sort: SortCrossedOffItems,
): List<ListRow> {
    val categoryNames: Map<String, String> = categories
        .mapNotNull { category -> category.categoryId?.let { it to category.name } }
        .toMap()
    val tagIdsByItem = tagLinks.groupBy({ it.listItemId }, { it.tagId })

    // null = uncategorized (no category, or the category no longer exists)
    fun categoryOf(item: EasyListsListItem): String? =
        item.categoryId?.let { categoryNames[it] }

    fun rowFor(item: EasyListsListItem): ListRow.Item {
        val linkedIds = item.listItemId?.let { tagIdsByItem[it] }.orEmpty()
        return ListRow.Item(
            item = item,
            tags = tags.filter { tag -> linkedIds.any { it == tag.tagId } },
        )
    }

    // named categories alphabetically, uncategorized last
    fun List<EasyListsListItem>.groupedByCategory(): List<Pair<String?, List<EasyListsListItem>>> {
        val groups = groupBy { categoryOf(it) }
        val named = groups.entries
            .filter { it.key != null }
            .sortedBy { it.key }
            .map { it.key to it.value }
        val uncategorized = groups[null]
            ?.let { listOf<Pair<String?, List<EasyListsListItem>>>(null to it) }
            .orEmpty()
        return named + uncategorized
    }

    val (crossedOffItems, openItems) = items.partition { it.crossedOff == true }
    val rows = mutableListOf<ListRow>()

    fun addGroup(name: String?, groupItems: List<EasyListsListItem>, crossedOff: Boolean) {
        rows += ListRow.CategoryHeader(name, crossedOff)
        groupItems.forEach { rows += rowFor(it) }
    }

    openItems.groupedByCategory().forEach { (name, groupItems) ->
        addGroup(name, groupItems, crossedOff = false)
    }

    if (crossedOffItems.isNotEmpty()) {
        fun List<EasyListsListItem>.sortedForCrossedOff() =
            if (sort == SortCrossedOffItems.MostRecentOnTop) sortedByDescending { it.crossedOffTimestamp }
            else sortedBy { it.name }

        when (group) {
            GroupCrossedOffItems.AllTogether -> {
                rows += ListRow.CrossedOffHeader
                crossedOffItems.sortedForCrossedOff().forEach { rows += rowFor(it) }
            }

            GroupCrossedOffItems.ByCategory ->
                crossedOffItems.groupedByCategory().forEach { (name, groupItems) ->
                    addGroup(name, groupItems.sortedForCrossedOff(), crossedOff = true)
                }
        }
        rows += ListRow.DeleteCrossedOff
    }

    return rows
}
//endregion
