package com.easylists.domain.common

enum class SortCrossedOffItems(val id: String) {
    Alphabetically("Alphabetically"),
    MostRecentOnTop("Most recently crossed off on top"),
    ;

    companion object {
        infix fun from(id: String?): SortCrossedOffItems? =
            SortCrossedOffItems.entries.firstOrNull { it.id == id }
    }
}
