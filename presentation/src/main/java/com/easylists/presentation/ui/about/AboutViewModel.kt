package com.easylists.presentation.ui.about

import androidx.lifecycle.ViewModel
import com.easylists.presentation.models.AppVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val appVersion: AppVersion
) : ViewModel() {

    // nothing to do here at this time

}
