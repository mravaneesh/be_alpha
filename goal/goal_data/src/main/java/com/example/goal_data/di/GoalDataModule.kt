package com.example.goal_data.di

import com.example.goal_data.repository.GoalRepositoryImpl
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.repository.GoalRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object GoalDataModule {
    @Provides
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideGoalRemoteDataSource(firestore: FirebaseFirestore): GoalRemoteDataSource {
        return GoalRemoteDataSource(firestore)
    }

    @Provides
    fun provideGoalRepository(dataSource: GoalRemoteDataSource): GoalRepository {
        return GoalRepositoryImpl(dataSource)
    }
}
