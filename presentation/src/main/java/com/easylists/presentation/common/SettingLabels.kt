package com.easylists.presentation.common

import androidx.annotation.StringRes
import com.easylists.domain.common.Capitalization
import com.easylists.domain.common.GroupCrossedOffItems
import com.easylists.domain.common.SortCrossedOffItems
import com.easylists.domain.common.ViewMode
import com.easylists.presentation.R

@StringRes
fun Capitalization.labelRes(): Int = when (this) {
    Capitalization.NoCapitalization -> R.string.capitalization_none
    Capitalization.CapitalizeFirstLetter -> R.string.capitalization_first_letter
    Capitalization.CapitalizeAllWords -> R.string.capitalization_all_words
}

@StringRes
fun GroupCrossedOffItems.labelRes(): Int = when (this) {
    GroupCrossedOffItems.AllTogether -> R.string.group_crossed_off_all_together
    GroupCrossedOffItems.ByCategory -> R.string.group_crossed_off_by_category
}

@StringRes
fun SortCrossedOffItems.labelRes(): Int = when (this) {
    SortCrossedOffItems.MostRecentOnTop -> R.string.sort_crossed_off_most_recent_on_top
    SortCrossedOffItems.Alphabetically -> R.string.sort_crossed_off_alphabetically
}

@StringRes
fun ViewMode.labelRes(): Int = when (this) {
    ViewMode.List -> R.string.list_view_mode
    ViewMode.Card -> R.string.card_view_mode
}