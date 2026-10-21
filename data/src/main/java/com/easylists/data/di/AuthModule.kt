package com.easylists.data.di

import com.easylists.data.repositories.AuthRepositoryImpl
import com.easylists.data.repositories.SyncRepositoryImpl
import com.easylists.domain.repositories.AuthRepository
import com.easylists.domain.repositories.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    companion object {

        // The Android emulator reaches the PC through this address
        private const val EMULATOR_HOST = "10.0.2.2"

        @Provides
        @Singleton
        fun provideFirebaseAuth(
            @Named("use_firebase_emulator") useEmulator: Boolean,
        ): FirebaseAuth = FirebaseAuth.getInstance().also {
            if (useEmulator) it.useEmulator(EMULATOR_HOST, 9099)
        }

        @Provides
        @Singleton
        fun provideFirestore(
            @Named("use_firebase_emulator") useEmulator: Boolean,
        ): FirebaseFirestore = FirebaseFirestore.getInstance().also {
            // Room is the offline store, so Firestore keeps its cache in memory only and
            // nothing from another account is left on disk
            it.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
            if (useEmulator) it.useEmulator(EMULATOR_HOST, 8080)
        }
    }

}
