package com.easylists.domain.repositories

import com.easylists.domain.models.ListAccess
import kotlinx.coroutines.flow.Flow

interface ListAccessRepository {
    // The user's access to every live list, keyed by list ID
    fun observeAccess(): Flow<Map<String, ListAccess>>
}
