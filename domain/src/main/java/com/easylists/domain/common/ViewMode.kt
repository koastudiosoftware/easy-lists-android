package com.easylists.domain.common

enum class ViewMode(val id: String) {
    Card("Card"),
    List("List"),
    ;

    companion object {
        infix fun from(id: String?): ViewMode? =
            ViewMode.entries.firstOrNull { it.id == id }
    }
}
