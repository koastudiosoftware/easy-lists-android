package com.easylists.data.di

import android.content.Context
import androidx.room.Room
import com.easylists.data.db.room.EasyListsDatabase
import com.easylists.data.db.room.dao.CategoryDao
import com.easylists.data.db.room.dao.ListDao
import com.easylists.data.db.room.dao.ListItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

//    @Provides
//    @Singleton
//    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
//        return PreferenceDataStoreFactory.create(
//            produceFile = {
//                context.preferencesDataStoreFile("app_settings")
//            }
//        )
//    }


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

}
