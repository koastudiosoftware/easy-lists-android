package com.easylists.presentation.common

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.toxicbakery.logging.Arbor


suspend fun fetchGoogleIdToken(context: Context, webClientId: String): String {
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
        .build()
    Arbor.i("Requesting credential")
    val response = CredentialManager.create(context).getCredential(context, request)
    Arbor.i("Credential returned")
    val credential = response.credential
    check(
        credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) { "Unexpected credential type" }
    return GoogleIdTokenCredential.createFrom(credential.data).idToken
}
