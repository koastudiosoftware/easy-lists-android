package com.easylists.data.di

import com.easylists.data.repositories.AppSettingsRepositoryImpl
import com.easylists.data.repositories.CategoryLocalDataSource
import com.easylists.data.repositories.CategoryRepositoryImpl
import com.easylists.data.repositories.ImageRepositoryImpl
import com.easylists.data.repositories.ListItemLocalDataSource
import com.easylists.data.repositories.ListItemRepositoryImpl
import com.easylists.data.repositories.ListLocalDataSource
import com.easylists.data.repositories.ListRepositoryImpl
import com.easylists.data.repositories.TagListItemLocalDataSource
import com.easylists.data.repositories.TagListItemRepositoryImpl
import com.easylists.data.repositories.TagLocalDataSource
import com.easylists.data.repositories.TagRepositoryImpl
import com.easylists.data.repositories.local.RoomCategoryLocalDataSource
import com.easylists.data.repositories.local.RoomListItemLocalDataSource
import com.easylists.data.repositories.local.RoomListLocalDataSource
import com.easylists.data.repositories.local.RoomTagListItemLocalDataSource
import com.easylists.data.repositories.local.RoomTagLocalDataSource
import com.easylists.domain.repositories.CategoryRepository
import com.easylists.domain.repositories.ImageRepository
import com.easylists.domain.repositories.ListItemRepository
import com.easylists.domain.repositories.ListRepository
import com.easylists.domain.repositories.SettingsRepository
import com.easylists.domain.repositories.TagListItemRepository
import com.easylists.domain.repositories.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // Settings
    @Binds
    abstract fun bindAppRepository(appSettingsRepository: AppSettingsRepositoryImpl): SettingsRepository


    // Category
    @Binds
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindCategoryLocalDataSource(roomCategoryLocalDataSource: RoomCategoryLocalDataSource): CategoryLocalDataSource


    // List
    @Binds
    abstract fun bindListRepository(listRepositoryImpl: ListRepositoryImpl): ListRepository

    @Binds
    abstract fun bindEventTypeLocalDataSource(roomEventTypeLocalDataSource: RoomListLocalDataSource): ListLocalDataSource


    // ListItem
    @Binds
    abstract fun bindListItemRepository(listItemRepositoryImpl: ListItemRepositoryImpl): ListItemRepository

    @Binds
    abstract fun bindListItemLocalDataSource(roomListItemLocalDataSource: RoomListItemLocalDataSource): ListItemLocalDataSource

    @Binds
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository


    // EasyListsTag
    @Binds
    abstract fun bindTagRepository(tagRepositoryImpl: TagRepositoryImpl): TagRepository

    @Binds
    abstract fun bindTagLocalDataSource(roomTagLocalDataSource: RoomTagLocalDataSource): TagLocalDataSource


    // TagListItem
    @Binds
    abstract fun bindTagListItemRepository(tagListItemRepositoryImpl: TagListItemRepositoryImpl): TagListItemRepository

    @Binds
    abstract fun bindTagListItemLocalDataSource(roomTagListItemLocalDataSource: RoomTagListItemLocalDataSource): TagListItemLocalDataSource

}
