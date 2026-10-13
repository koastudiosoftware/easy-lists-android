package com.easylists.domain.common

enum class Capitalization(val id: String) {
    NoCapitalization("none"),
    CapitalizeFirstLetter("first_letter"),
    CapitalizeAllWords("all_words");

    companion object {
        fun from(id: String?): Capitalization? = entries.firstOrNull { it.id == id }
    }
}