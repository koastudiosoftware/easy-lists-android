package com.easylists.presentation.ui.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylists.domain.models.ListNotice
import com.easylists.domain.use_cases.ClearListNoticesUseCase
import com.easylists.domain.use_cases.ObserveListNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListNoticeViewModel @Inject constructor(
    observeNotices: ObserveListNoticesUseCase,
    private val clearNotices: ClearListNoticesUseCase,
) : ViewModel() {

    // Notices that haven't been shown. Eagerly, so one that arrived while the
    // app was in the background is ready the moment a screen appears.
    val notices: StateFlow<List<ListNotice>> = observeNotices()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Clears only what was on screen
    fun onDismiss(shown: List<ListNotice>) {
        viewModelScope.launch { clearNotices(shown.map { it.id }) }
    }
}
