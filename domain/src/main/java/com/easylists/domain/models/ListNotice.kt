package com.easylists.domain.models

//
// A notice is something the app tells the person once, with a popup, about a
// shared list:
//   Gone: the list was taken away from this person. The owner deleted it or
//         removed the person; to the person it is the same thing.
//   Left: someone left a list this person owns, on their own. Without this the
//         owner could assume the person is still on the list.
//
// The sync writes a notice when it sees the change, and the popup reads it
// later. It is saved, not just shown, because the app can learn about a change
// while it is closed, with no screen to show anything on.
//

enum class NoticeKind { Gone, Left }

data class ListNotice(
    val id: String,
    val kind: NoticeKind,
    val listId: String,
    val listName: String,
    // For Left: who left. Null when their name isn't known.
    val personName: String?,
)
