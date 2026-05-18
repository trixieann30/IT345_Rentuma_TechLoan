package com.example.techloan.features.reservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.BorrowRequestDto
import com.example.techloan.shared.model.CreateBorrowRequestDto
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

sealed class ReservationState {
    object Idle : ReservationState()
    object Loading : ReservationState()
    data class SubmitSuccess(val reservation: BorrowRequestDto) : ReservationState()
    data class ListSuccess(val items: List<BorrowRequestDto>) : ReservationState()
    data class Error(val message: String) : ReservationState()
}

class ReservationViewModel : ViewModel() {

    private val _state = MutableLiveData<ReservationState>(ReservationState.Idle)
    val state: LiveData<ReservationState> = _state

    fun submitReservation(token: String, dto: CreateBorrowRequestDto) {
        _state.value = ReservationState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.createReservation(token, dto)
                if (res.isSuccessful && res.body() != null) {
                    _state.value = ReservationState.SubmitSuccess(res.body()!!)
                } else {
                    val err = res.errorBody()?.string() ?: ""
                    _state.value = ReservationState.Error("Failed to submit (${res.code()}): $err")
                }
            } catch (e: Exception) {
                _state.value = ReservationState.Error(networkErrorMessage(e))
            }
        }
    }

    fun loadMyReservations(token: String, userId: Long) {
        _state.value = ReservationState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getMyReservations(token, userId)
                if (res.isSuccessful) {
                    _state.value = ReservationState.ListSuccess(res.body() ?: emptyList())
                } else {
                    _state.value = ReservationState.Error("Failed to load reservations (${res.code()})")
                }
            } catch (e: Exception) {
                _state.value = ReservationState.Error(networkErrorMessage(e))
            }
        }
    }
}
