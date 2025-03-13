package com.easylists.presentation.models

import com.easylists.domain.models.EasyListsList

data class ListOfListsState(
    var isPullToRefreshing: Boolean = false,
    var listList: List<EasyListsList>? = null,
    var uiState: ListListUiState = ListListUiState.Idle
)


sealed interface ListListUiState {
    object Idle : ListListUiState
    data class Refreshing(val isAutomaticRefresh: Boolean) : ListListUiState
    data class Error(val message: String) : ListListUiState
}
