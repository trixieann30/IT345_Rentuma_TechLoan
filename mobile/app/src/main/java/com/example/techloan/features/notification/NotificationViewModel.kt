package com.example.techloan.features.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.NotificationDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class NotificationState {
    object Loading : NotificationState()
    data class Success(val items: List<NotificationDto>) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

class NotificationViewModel : ViewModel() {

    private val _state = MutableLiveData<NotificationState>()
    val state: LiveData<NotificationState> = _state

    private val _unreadCount = MutableLiveData(0L)
    val unreadCount: LiveData<Long> = _unreadCount

    fun load(token: String) {
        _state.value = NotificationState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getNotifications(token)
                if (res.isSuccessful) {
                    _state.value = NotificationState.Success(res.body() ?: emptyList())
                } else {
                    _state.value = NotificationState.Error("Failed to load notifications")
                }
            } catch (e: Exception) {
                _state.value = NotificationState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadUnreadCount(token: String) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getUnreadCount(token)
                if (res.isSuccessful) {
                    _unreadCount.value = res.body()?.count ?: 0L
                }
            } catch (_: Exception) {}
        }
    }

    fun markRead(token: String, id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.markNotificationRead(token, id)
                val current = (_state.value as? NotificationState.Success)?.items ?: return@launch
                _state.value = NotificationState.Success(
                    current.map { if (it.id == id) it.copy(read = true) else it }
                )
                _unreadCount.value = (_unreadCount.value ?: 1) - 1
            } catch (_: Exception) {}
        }
    }

    fun markAllRead(token: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.markAllNotificationsRead(token)
                val current = (_state.value as? NotificationState.Success)?.items ?: return@launch
                _state.value = NotificationState.Success(current.map { it.copy(read = true) })
                _unreadCount.value = 0L
            } catch (_: Exception) {}
        }
    }
}
