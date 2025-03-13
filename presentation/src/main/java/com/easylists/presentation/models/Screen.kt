package com.easylists.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Screen : Parcelable {

    @Parcelize
    object ListOfLists : Screen

    @Parcelize
    object Settings : Screen

    @Parcelize
    object About : Screen

}