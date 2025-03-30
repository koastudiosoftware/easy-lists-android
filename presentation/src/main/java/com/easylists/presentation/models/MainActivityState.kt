package com.easylists.presentation.models

import com.easylists.domain.common.Themes

data class MainActivityState(
    var theme: Themes = Themes.Solarized,
)
