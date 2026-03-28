package com.example.techloan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.api.RetrofitClient
import com.example.techloan.model.LoginRequest
import com.example.techloan.model.RegisterRequest
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String, val message: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(name = name, email = email, password = password)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token ?: ""
                    _authState.value = AuthState.Success(token, "Registration successful!")
                } else {
                    val errMsg = response.errorBody()?.string()
                        ?.let { parseErrorMessage(it) }
                        ?: "Registration failed. Please try again."
                    _authState.value = AuthState.Error(errMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Network error: ${e.localizedMessage ?: "Please check your connection."}"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(email = email, password = password)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token ?: ""
                    _authState.value = AuthState.Success(token, "Login successful!")
                } else {
                    val errMsg = when (response.code()) {
                        401 -> "Invalid email or password."
                        404 -> "Account not found. Please register first."
                        else -> response.errorBody()?.string()
                            ?.let { parseErrorMessage(it) }
                            ?: "Login failed. Please try again."
                    }
                    _authState.value = AuthState.Error(errMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Network error: ${e.localizedMessage ?: "Please check your connection."}"
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            // Try to extract "message" from JSON error body
            val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            regex.find(errorBody)?.groupValues?.get(1) ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}