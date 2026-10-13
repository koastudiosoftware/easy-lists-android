package com.easylists.domain.common

enum class AppSettingsType(val type: String) {
    Boolean("Boolean"),
    Long("Long"),
    String("String"),
    ;

    override fun toString(): String {
        return type
    }
}
