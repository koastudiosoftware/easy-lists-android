package com.easylists.domain.models

//region Themes
enum class Themes(val value: String) {
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