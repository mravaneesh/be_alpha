package com.example.goal_data.di

import android.content.Context
import androidx.room.Room
import com.example.goal_data.db.GoalDao
import com.example.goal_data.db.GoalDatabase
import com.example.goal_data.repository.GoalRepositoryImpl
import com.example.goal_data.source.FirestoreGoalRemoteDataSource
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_data.sync.GoalSyncer
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.sync.SyncScheduler
import com.example.goal_domain.sync.GoalSynchronizer
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
        FirestoreGoalRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideGoalDatabase(@ApplicationContext context: Context): GoalDatabase =
        Room.databaseBuilder(
            context,
            GoalDatabase::class.java,
            "apogee_goals.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideGoalDao(database: GoalDatabase): GoalDao = database.goalDao()

    @Provides
    @Singleton
    fun provideGoalRepository(
        remote: GoalRemoteDataSource,
        dao: GoalDao,
        db: GoalDatabase,
        scheduler: SyncScheduler,
    ): GoalRepository = GoalRepositoryImpl(remote, dao, db, scheduler)

    /** :app injects the GoalSynchronizer interface; the implementation stays in this module. */
    @Provides
    @Singleton
    fun provideGoalSynchronizer(
        dao: GoalDao,
        remote: GoalRemoteDataSource,
    ): GoalSynchronizer = GoalSyncer(dao, remote)
}
