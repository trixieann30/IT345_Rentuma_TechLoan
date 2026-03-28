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

    fun register(fullName: String, email: String, studentId: String, password: String, confirmPassword: String, role: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        studentId = studentId,
                        password = password,
                        confirmPassword = confirmPassword,
                        role = role
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token ?: ""
                    _authState.value = AuthState.Success(token, "Registration successful!")
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val errMsg = when (response.code()) {
                        400 -> "Validation error: $errorBody"
                        409 -> "Email or Student ID already registered: $errorBody"
                        500 -> "Server error: $errorBody"
                        else -> "Registration failed (${response.code()}): $errorBody"
                    }
                    _authState.value = AuthState.Error(errMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Network error: ${e.message ?: e.localizedMessage ?: "Please check your connection."}"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        
        android.util.Log.d("AUTH_LOGIN", "Attempting login - Email: $email, Password length: ${password.length}")

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(email = email, password = password)
                )
                android.util.Log.d("AUTH_LOGIN", "Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token ?: ""
                    android.util.Log.d("AUTH_LOGIN", "Login successful, token: ${token.take(20)}...")
                    _authState.value = AuthState.Success(token, "Login successful!")
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    android.util.Log.d("AUTH_LOGIN", "Login failed - Error body: $errorBody")
                    
                    val errMsg = when (response.code()) {
                        400 -> "Bad request: $errorBody"
                        401 -> "Invalid email or password. Check your credentials."
                        404 -> "Account not found with this email"
                        500 -> "Server error: $errorBody"
                        else -> "Error (${response.code()}): $errorBody"
                    }
                    _authState.value = AuthState.Error(errMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_LOGIN", "Exception: ${e.message}", e)
                _authState.value = AuthState.Error(
                    "Network error: ${e.message ?: e.localizedMessage ?: "Please check your connection."}"
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
            val found = regex.find(errorBody)?.groupValues?.get(1)
            if (found != null) {
                found
            } else {
                // If no message field found, try to return the whole body for debugging
                if (errorBody.length > 100) errorBody.substring(0, 100) else errorBody
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}