package com.easylists.presentation.models

import com.easylists.domain.models.Themes

data class MainActivityState(
    var theme: Themes = Themes.Solarized,
)
