package com.easylists.domain.common

//region AppSettingsType
enum class AppSettingsType(val type: String) {
    Boolean("Boolean"),
    Long("Long"),
    String("String"),
    ;

    override fun toString(): String {
        return type
    }
}
//endregion
