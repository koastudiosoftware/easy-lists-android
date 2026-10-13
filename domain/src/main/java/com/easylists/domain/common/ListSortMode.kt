package com.easylists.domain.common

enum class ListSortMode(val id: String) {
    NameAsc("name_asc"),
    NameDesc("name_desc"),
    Manual("manual");

    companion object {
        fun from(id: String?): ListSortMode? = entries.firstOrNull { it.id == id }
    }
}
