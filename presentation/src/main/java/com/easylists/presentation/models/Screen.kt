package com.easylists.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Screen : Parcelable {

    @Parcelize
    object Categories : Screen

    @Parcelize
    object Tags : Screen

    @Parcelize
    object Lists : Screen

    @Parcelize
    data class ListItems(val listId: String, val listName: String) : Screen

    @Parcelize
    object Settings : Screen

    @Parcelize
    object About : Screen

}