package com.easylists.data.share

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// the token of an invitation link that was opened and isn't dealt with yet
@Singleton
class PendingInviteStore @Inject constructor() {

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun set(token: String) {
        _token.value = token
    }

    fun clear() {
        _token.value = null
    }
}
