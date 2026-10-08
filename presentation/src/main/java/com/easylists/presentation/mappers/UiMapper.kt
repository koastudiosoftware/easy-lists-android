package com.easylists.presentation.mappers

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.easylists.domain.common.Capitalization
import com.easylists.domain.exceptions.TemporarilyUnavailableNetworkServiceException
import com.easylists.presentation.BuildConfig
import com.easylists.presentation.R
import com.toxicbakery.logging.Arbor
import java.io.IOException
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject

class UiMapper @Inject constructor() {

    //region mapErrorToUiMessage()
    fun mapErrorToUiMessage(error: Throwable): String {
        Arbor.e("ERROR: $error \n ${error.printStackTrace()}")

        return when (error) {
            is TemporarilyUnavailableNetworkServiceException -> "The ${error.serviceName} service is now at capacity. Please try again in a minute."

            is UnknownHostException,
            is SocketException,
            is IOException -> {
                "You appear to be offline. Please, check your internet connection and retry."
            }

            else -> if (BuildConfig.DEBUG) {
                error.toString()
            } else {
                "There has been an error while retrieving data. Please try again later."
            }
        }
    }
    //endregion


    fun Capitalization.toKeyboardCapitalization(): KeyboardCapitalization = when (this) {
        Capitalization.NoCapitalization -> KeyboardCapitalization.None
        Capitalization.CapitalizeFirstLetter -> KeyboardCapitalization.Sentences
        Capitalization.CapitalizeAllWords -> KeyboardCapitalization.Words
    }

    @StringRes
    fun Capitalization.labelRes(): Int = when (this) {
        Capitalization.NoCapitalization -> R.string.capitalization_none
        Capitalization.CapitalizeFirstLetter -> R.string.capitalization_first_letter
        Capitalization.CapitalizeAllWords -> R.string.capitalization_all_words
    }

}