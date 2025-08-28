package com.example.notification_data.di

import android.content.Context
import androidx.room.Room
import com.example.notification_data.local.NotificationDatabase
import com.example.notification_data.repository.NotificationRepositoryImpl
import com.example.notification_domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationDataModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): NotificationDatabase {
        return Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            "notification_db"
        ).build()
    }

    @Provides
    fun provideDao(db: NotificationDatabase) = db.notificationDao()

    @Provides
    @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository = impl
}