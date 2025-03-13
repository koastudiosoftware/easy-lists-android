package com.easylists.presentation.mappers

import com.easylists.domain.exceptions.TemporarilyUnavailableNetworkServiceException
import com.easylists.presentation.BuildConfig
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

}