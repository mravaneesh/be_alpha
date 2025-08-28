package com.example.notification_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notification_domain.model.Notification
import com.example.notification_domain.usecase.GetNotificationUseCase
import com.example.notification_domain.usecase.SyncNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    getNotificationsUseCase: GetNotificationUseCase,
    private val syncNotificationsUseCase: SyncNotificationsUseCase
): ViewModel() {

    val notifications: StateFlow<List<Notification>> = getNotificationsUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun sync(userId: String) {
        viewModelScope.launch {
            syncNotificationsUseCase(userId)
        }
    }
}