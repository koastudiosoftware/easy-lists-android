package com.easylists.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import coil3.ImageLoader
import coil3.imageLoader
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import com.easylists.data.db.room.dao.TagDao
import com.easylists.data.db.room.dao.TagListItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("app_settings")
            }
        )
    }


    @Provides
    @Singleton
    fun provideCategoryDao(
        easyListsDatabase: EasyListsDatabase
    ): CategoryDao = easyListsDatabase.categoryDao()


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EasyListsDatabase {
        return Room.databaseBuilder(context, EasyListsDatabase::class.java, "easy-lists.db")
            .createFromAsset("easy-lists.db")
            .build()
    }


    @Provides
    @Singleton
    fun provideListDao(
        easyListsDatabase: EasyListsDatabase
    ): ListDao = easyListsDatabase.listDao()


    @Provides
    @Singleton
    fun provideListItemDao(
        easyListsDatabase: EasyListsDatabase
    ): ListItemDao = easyListsDatabase.listItemDao()


    @Provides
    @Singleton
    fun provideTagDao(
        easyListsDatabase: EasyListsDatabase
    ): TagDao = easyListsDatabase.tagDao()


    @Provides
    @Singleton
    fun provideTagListItemDao(
        easyListsDatabase: EasyListsDatabase
    ): TagListItemDao = easyListsDatabase.tagListItemDao()


    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return context.imageLoader // Coil3's Context extension, returns the singleton
    }

}
