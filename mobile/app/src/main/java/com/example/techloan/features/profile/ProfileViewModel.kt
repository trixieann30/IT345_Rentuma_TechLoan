package com.example.techloan.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.PenaltySummaryDto
import com.example.techloan.shared.model.UserDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private fun networkErrorMessage(e: Exception) = when {
    e is SocketTimeoutException || e.message?.contains("timeout", true) == true ->
        "Server is starting up. Please wait a moment and try again."
    e is UnknownHostException -> "No internet connection. Please check your network."
    else -> "Connection failed. Please try again."
}

data class ProfileData(val user: UserDto, val penalties: PenaltySummaryDto)

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val data: ProfileData) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val _state = MutableLiveData<ProfileState>()
    val state: LiveData<ProfileState> = _state

    fun loadProfile(token: String, userId: Long) {
        _state.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val userRes    = RetrofitClient.api.getMe(token)
                val penaltyRes = RetrofitClient.api.getUserPenalties(token, userId)

                val user = userRes.body()
                if (userRes.isSuccessful && user != null) {
                    _state.value = ProfileState.Success(ProfileData(
                        user      = user,
                        penalties = penaltyRes.body() ?: PenaltySummaryDto()
                    ))
                } else {
                    _state.value = ProfileState.Error("Failed to load profile (${userRes.code()})")
                }
            } catch (e: Exception) {
                _state.value = ProfileState.Error(networkErrorMessage(e))
            }
        }
    }
}
