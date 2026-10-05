package com.easylists.data.repositories

import com.easylists.data.db.room.dao.UserDao
import com.easylists.domain.repositories.SessionRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SessionRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : SessionRepository {

    override suspend fun getUserId(): String = withTimeout(5.seconds) {
        userDao.observeUserId().filterNotNull().first()
    }

}
