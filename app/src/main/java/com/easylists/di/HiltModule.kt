package com.easylists.di

import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DefaultDispatcherProvider
import com.github.davidepanidev.kotlinextensions.utils.dispatchers.DispatcherProvider
import com.github.davidepanidev.kotlinextensions.utils.serialization.SerializationManager
import com.github.davidepanidev.kotlinextensions.utils.serialization.gson.GsonSerializationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    @Provides
    @Singleton
    fun provideSerializationManager(): SerializationManager {
        return GsonSerializationManager()
    }

    @Provides
    @Singleton
    fun providerDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

}
