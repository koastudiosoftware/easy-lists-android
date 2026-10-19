package com.easylists.di

import android.content.Context
import com.easylists.BuildConfig
import com.easylists.presentation.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AuthConfigModule {

    @Provides
    @Named("web_client_id")
    fun provideWebClientId(@ApplicationContext context: Context): String =
        context.getString(R.string.default_web_client_id)

    // True only in debug builds, so release builds never point at an emulator
    @Provides
    @Named("use_firebase_emulator")
    fun provideUseFirebaseEmulator(): Boolean = BuildConfig.DEBUG
}
