package com.example.profile_data.di

import com.example.profile_data.repository.ProfileRepositoryImpl
import com.example.profile_data.source.ProfileRemoteDataSource
import com.example.profile_domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
object ProfileDataModule {
    @Provides
    fun provideProfileRemoteDataSource(firestore: FirebaseFirestore): ProfileRemoteDataSource {
        return ProfileRemoteDataSource(firestore)
    }

    @Provides
    fun provideProfileRepository(dataSource: ProfileRemoteDataSource): ProfileRepository {
        return ProfileRepositoryImpl(dataSource)
    }
}
