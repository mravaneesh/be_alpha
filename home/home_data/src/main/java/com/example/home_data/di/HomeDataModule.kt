package com.example.home_data.di

import com.example.home_data.repository.PostRepositoryImpl
import com.example.home_data.source.PostRemoteDataSource
import com.example.home_domain.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object HomeDataModule {
    @Provides
    fun provideGoalRemoteDataSource(firestore: FirebaseFirestore): PostRemoteDataSource {
        return PostRemoteDataSource(firestore)
    }

    @Provides
    fun provideGoalRepository(dataSource: PostRemoteDataSource): PostRepository {
        return PostRepositoryImpl(dataSource)
    }
}