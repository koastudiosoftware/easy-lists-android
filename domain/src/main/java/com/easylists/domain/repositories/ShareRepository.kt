package com.easylists.domain.repositories

interface ShareRepository {

    suspend fun prepareListForSharing(listId: String)

    // Debug rows only
    suspend fun firstOwnedListId(): String?

}
