package com.easylists.data.di

import com.easylists.data.repositories.ListLocalDataSource
import com.easylists.data.repositories.ListRepositoryImpl
import com.easylists.data.repositories.local.RoomListLocalDataSource
import com.easylists.domain.repositories.ListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // Settings
//    @Binds
//    abstract fun bindAppRepository(appSettingsRepository: AppSettingsRepository): SettingsRepository


    // Category
//    @Binds
//    abstract fun bindEventRepository(eventRepositoryImpl: EventRepositoryImpl): EventRepository
//
//    @Binds
//    abstract fun bindEventLocalDataSource(roomEventLocalDataSource: RoomEventLocalDataSource): EventLocalDataSource


    // List
    @Binds
    abstract fun bindListRepository(listRepositoryImpl: ListRepositoryImpl): ListRepository

    @Binds
    abstract fun bindEventTypeLocalDataSource(roomEventTypeLocalDataSource: RoomListLocalDataSource): ListLocalDataSource


    // ListItem
//    @Binds
//    abstract fun bindPortfolioRepository(portfolioRepositoryImpl: PortfolioRepositoryImpl): PortfolioRepository
//
//    @Binds
//    abstract fun bindPortfolioLocalDataSource(roomPortfolioLocalDataSource: RoomPortfolioLocalDataSource): PortfolioLocalDataSource

}