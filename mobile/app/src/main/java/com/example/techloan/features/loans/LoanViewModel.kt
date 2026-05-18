package com.example.techloan.features.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.LoanDto
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

sealed class LoanState {
    object Loading : LoanState()
    data class Success(val loans: List<LoanDto>) : LoanState()
    data class Error(val message: String) : LoanState()
}

class LoanViewModel : ViewModel() {

    private val _state = MutableLiveData<LoanState>()
    val state: LiveData<LoanState> = _state

    fun loadMyLoans(token: String, userId: Long) {
        _state.value = LoanState.Loading
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getMyLoans(token, userId)
                if (res.isSuccessful) {
                    _state.value = LoanState.Success(res.body() ?: emptyList())
                } else {
                    _state.value = LoanState.Error("Failed to load loans (${res.code()})")
                }
            } catch (e: Exception) {
                _state.value = LoanState.Error(networkErrorMessage(e))
            }
        }
    }
}
