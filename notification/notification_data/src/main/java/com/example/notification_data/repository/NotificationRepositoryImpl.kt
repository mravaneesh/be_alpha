package com.example.notification_data.repository

import com.example.goal_domain.repository.GoalRepository
import com.example.notification_data.mapper.toDomain
import com.example.notification_data.mapper.toEntity
import com.example.notification_data.mapper.toReminderEntities
import com.example.notification_data.source.NotificationLocalDataSource
import com.example.notification_data.source.NotificationRemoteSource
import com.example.notification_domain.model.Notification
import com.example.notification_domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val localDataSource: NotificationLocalDataSource,
    private val remoteDataSource: NotificationRemoteSource,
    private val goalRepository: GoalRepository
): NotificationRepository {
    override suspend fun getNotifications(): Flow<List<Notification>> {
        return localDataSource.getAll().map { list ->
            list.map {it.toDomain()}
        }
    }

    override suspend fun syncNotifications(userId: String) {
        val goals = goalRepository.getGoals(userId,"habit")
        val reminders = goals.mapNotNull { it.toReminderEntities() }
        val socials = remoteDataSource.getSocialNotifications(userId).map {it.toEntity()}

        localDataSource.savaAll(reminders+socials)
    }

}