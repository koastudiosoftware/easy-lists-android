package com.easylists.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Screen : Parcelable {

    @Parcelize
    object EditCategories : Screen

    @Parcelize
    object EditTags : Screen

    @Parcelize
    object MasterLists : Screen

    @Parcelize
    object ListDetails : Screen

    @Parcelize
    object Settings : Screen

    @Parcelize
    object About : Screen

}