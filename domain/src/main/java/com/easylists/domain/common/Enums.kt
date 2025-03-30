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


//region Themes
enum class Themes(val value: String) {
    Merlot("Merlot"),
    Nautical("Nautical"),
    Ocean("Ocean"),
    Slate("Slate"),
    Solarized("Solarized (Default)"),
    TropicalFoliage("Tropical Foliage")
    ;

    override fun toString(): String {
        return value
    }

    companion object {
        val Default = Solarized

        infix fun from(value: String): Themes? = Themes.entries.firstOrNull { it.value == value }
    }
}
//endregion
