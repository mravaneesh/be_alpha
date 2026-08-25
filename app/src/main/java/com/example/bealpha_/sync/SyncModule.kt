package com.example.bealpha_.sync

import com.example.goal_domain.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the WorkManager-backed scheduler. This is the only place :app is named as the owner of
 * background sync — goal_data just asks a [SyncScheduler] for a pass.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler
}
