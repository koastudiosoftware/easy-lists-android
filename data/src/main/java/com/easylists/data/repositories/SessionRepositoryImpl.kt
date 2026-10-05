package com.easylists.data.repositories

import com.easylists.data.db.room.dao.UserDao
import com.easylists.domain.repositories.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : SessionRepository {

    override suspend fun getUserId(): String = userDao.getUserId()

}
