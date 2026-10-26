package com.easylists.domain.models

object InviteLinks {

    // Your Firebase project ID; Hosting serves <id>.web.app for free
    private const val BASE_URL = "https://easy-lists-android-d3c24.web.app"
    private const val PATH = "/invite/"

    fun linkFor(token: String): String = "$BASE_URL$PATH$token"

    // The token inside an invite link, or null if the text isn't one
    fun tokenFrom(url: String): String? {
        if (!url.startsWith("$BASE_URL$PATH")) return null
        return url.removePrefix("$BASE_URL$PATH")
            .substringBefore('?')
            .substringBefore('#')
            .takeIf { it.isNotEmpty() }
    }

}
