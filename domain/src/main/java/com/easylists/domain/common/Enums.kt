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


//region Capitalization
enum class Capitalization(val id: String) {
    NoCapitalization("none"),
    CapitalizeFirstLetter("first_letter"),
    CapitalizeAllWords("all_words");

    companion object {
        fun from(id: String?): Capitalization? = entries.firstOrNull { it.id == id }
    }
}
//endregion


//region GroupCrossedOffItems
enum class GroupCrossedOffItems(val id: String) {
    AllTogether("All together"),
    ByCategory("By category"),
    ;

    companion object {
        infix fun from(id: String?): GroupCrossedOffItems? =
            GroupCrossedOffItems.entries.firstOrNull { it.id == id }
    }
}
//endregion


//region SortCrossedOffItems
enum class SortCrossedOffItems(val id: String) {
    Alphabetically("Alphabetically"),
    MostRecentOnTop("Most recently crossed off on top"),
    ;

    companion object {
        infix fun from(id: String?): SortCrossedOffItems? =
            SortCrossedOffItems.entries.firstOrNull { it.id == id }
    }
}
//endregion


//region Themes
enum class Themes(val id: String) {
    Merlot("Merlot"),
    Nautical("Nautical"),
    Ocean("Ocean"),
    Slate("Slate"),
    Solarized("Solarized (Default)"),
    TropicalFoliage("Tropical Foliage")
    ;

    companion object {
        val Default = Solarized

        infix fun from(id: String?): Themes = Themes.entries.firstOrNull { it.id == id } ?: Default
    }
}
//endregion


//region ViewMode
enum class ViewMode(val id: String) {
    Card("Card"),
    List("List"),
    ;

    companion object {
        infix fun from(id: String?): ViewMode? =
            ViewMode.entries.firstOrNull { it.id == id }
    }
}
//endregion
