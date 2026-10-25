package com.easylists.domain.models

// What the signed-in user is allowed to do on one list.
//
// owner:  everything, including renaming, deleting and sharing the list
// editor: items, tags on items and categories on the list, but not the list itself
// viewer: read only
enum class ListRole {
    Owner,
    Editor,
    Viewer
}

data class ListAccess(
    val role: ListRole,
    // true once the list has been prepared for sharing
    val isShared: Boolean,
) {
    // Add, edit, cross off and delete items
    val canEditItems: Boolean get() = role != ListRole.Viewer

    // Rename, delete, share and manage the members of the list
    val canManageList: Boolean get() = role == ListRole.Owner
}
