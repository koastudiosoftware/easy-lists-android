package com.easylists.domain.repositories

import com.easylists.domain.models.AuthAccount
import com.easylists.domain.models.LinkResult

interface SessionRepository {

    suspend fun getUserId(): String

    suspend fun linkAccount(account: AuthAccount): LinkResult

    suspend fun switchAccount(account: AuthAccount)

    suspend fun removeSharedListsFromDevice()

}
