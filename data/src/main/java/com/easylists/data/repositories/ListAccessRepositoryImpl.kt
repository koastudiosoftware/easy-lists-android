package com.easylists.data.repositories

import com.easylists.data.db.room.dao.ShareDao
import com.easylists.domain.models.ListAccess
import com.easylists.domain.models.ListRole
import com.easylists.domain.repositories.ListAccessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// The role comes from the member rows, which are also on the device when offline.
// The query finds the current user itself, so the result follows the user's ID
// when the first sign-in changes it.
class ListAccessRepositoryImpl @Inject constructor(
    private val shareDao: ShareDao,
) : ListAccessRepository {

    override fun observeAccess(): Flow<Map<String, ListAccess>> {
        return shareDao.observeListAccess().map { rows ->
            rows.associate { row ->
                row.listId to ListAccess(
                    role = when (row.role) {
                        "owner" -> ListRole.Owner
                        "editor" -> ListRole.Editor
                        else -> ListRole.Viewer
                    },
                    isShared = row.isShared,
                )
            }
        }
    }
}
