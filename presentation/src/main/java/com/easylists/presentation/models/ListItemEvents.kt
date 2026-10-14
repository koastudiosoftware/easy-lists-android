package com.easylists.presentation.models

sealed interface ListItemsEvent {
    data class CrossedOffItemsDeleted(val itemIds: List<String>) : ListItemsEvent
}
