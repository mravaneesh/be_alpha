package com.example.goal_data.di

import android.content.Context
import androidx.room.Room
import com.example.goal_data.db.GoalDao
import com.example.goal_data.db.GoalDatabase
import com.example.goal_data.repository.GoalRepositoryImpl
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.repository.GoalRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object GoalDataModule {

    @Provides
    fun provideGoalRemoteDataSource(firestore: FirebaseFirestore): GoalRemoteDataSource =
        GoalRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideGoalDatabase(@ApplicationContext context: Context): GoalDatabase =
        Room.databaseBuilder(context, GoalDatabase::class.java, "apogee_goals.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGoalDao(database: GoalDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideGoalRepository(
        remote: GoalRemoteDataSource,
        dao: GoalDao,
    ): GoalRepository = GoalRepositoryImpl(remote, dao)
}
