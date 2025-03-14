package com.easylists.presentation.common


//region isNumeric
fun isNumeric(str: String): Boolean = str
    .removePrefix("-")
    .removePrefix("+")
    .all { it in '0'..'9' }
//endregion
