package com.easylists.domain.models

sealed interface LinkResult {
    data object Linked : LinkResult          // first sign-in: local data now belongs to the account
    data object AlreadyLinked : LinkResult   // same account as before
    data object SwitchRequired : LinkResult  // a different account: needs the warning
}
