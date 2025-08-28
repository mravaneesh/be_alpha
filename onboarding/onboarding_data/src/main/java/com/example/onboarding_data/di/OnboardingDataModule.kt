package com.example.onboarding_data.di

import com.example.onboarding_data.repository.UserPreferencesRepositoryImpl
import com.example.onboarding_data.source.OnboardingRemoteDataSource
import com.example.onboarding_domain.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingDataModule {

    @Provides
    @Singleton
    fun provideOnboardingRemoteDataSource(
        firestore: FirebaseFirestore
    ): OnboardingRemoteDataSource = OnboardingRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        remote: OnboardingRemoteDataSource
    ): UserPreferencesRepository = UserPreferencesRepositoryImpl(remote)
}