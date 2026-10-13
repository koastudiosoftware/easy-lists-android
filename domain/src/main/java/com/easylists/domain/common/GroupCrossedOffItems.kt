package com.easylists.domain.common

enum class GroupCrossedOffItems(val id: String) {
    AllTogether("All together"),
    ByCategory("By category"),
    ;

    companion object {
        infix fun from(id: String?): GroupCrossedOffItems? =
            GroupCrossedOffItems.entries.firstOrNull { it.id == id }
    }
}
