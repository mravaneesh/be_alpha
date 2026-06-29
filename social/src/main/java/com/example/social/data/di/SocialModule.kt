package com.example.social.data.di

import com.example.social.data.ChallengeRepositoryImpl
import com.example.social.data.SocialRepositoryImpl
import com.example.social.domain.repository.ChallengeRepository
import com.example.social.domain.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SocialModule {

    // FirebaseFirestore is already provided app-wide by CommonAppModule (:common:utils).
    @Provides @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideSocialRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
    ): SocialRepository = SocialRepositoryImpl(db, auth)

    @Provides @Singleton
    fun provideChallengeRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
    ): ChallengeRepository = ChallengeRepositoryImpl(db, auth)
}
