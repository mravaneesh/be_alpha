package com.example.home_domain.di

import com.example.home_domain.repository.PostRepository
import com.example.home_domain.usecase.GetFeedPostUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object HomeDomainModule {

    @Provides
    @Singleton
    fun provideGetFeedPostsUseCase(
        repository: PostRepository
    ): GetFeedPostUseCase {
        return GetFeedPostUseCase(repository)
    }
}