package com.example.techloan.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techloan.shared.model.ForgotPasswordRequest
import com.example.techloan.shared.model.GoogleAuthRequestDto
import com.example.techloan.shared.model.LoginRequest
import com.example.techloan.shared.model.RegisterRequest
import com.example.techloan.shared.model.UserDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String, val message: String?, val user: UserDto? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _forgotPasswordState = MutableLiveData<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: LiveData<ForgotPasswordState> = _forgotPasswordState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(email = email, password = password))
                if (response.isSuccessful) {
                    val body = response.body()
                    _authState.value = AuthState.Success(body?.token ?: "", "Login successful!", body?.user)
                } else {
                    val msg = when (response.code()) {
                        401 -> "Invalid email or password."
                        403 -> "Please verify your email before logging in."
                        else -> "Login failed (${response.code()})"
                    }
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.googleSignIn(
                    GoogleAuthRequestDto(idToken = idToken, role = "STUDENT")
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    _authState.value = AuthState.Success(body?.token ?: "", "Sign-in successful!", body?.user)
                } else {
                    _authState.value = AuthState.Error("Sign-in failed: check your Google account is @cit.edu")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    fun register(fullName: String, email: String, studentId: String, password: String, confirmPassword: String, role: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(fullName, email, studentId, password, confirmPassword, role)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    _authState.value = AuthState.Success(body?.token ?: "", "Registration successful!")
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = when (response.code()) {
                        400 -> "Validation error: $errorBody"
                        409 -> "Email or Student ID already registered."
                        else -> "Registration failed (${response.code()})"
                    }
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    fun forgotPassword(email: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _forgotPasswordState.value = ForgotPasswordState.Success(
                        "If that email is registered, a reset link has been sent."
                    )
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.Error("Request failed. Please try again.")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Network error: ${e.message}")
            } finally {
                _forgotPasswordState.value = ForgotPasswordState.Idle
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
