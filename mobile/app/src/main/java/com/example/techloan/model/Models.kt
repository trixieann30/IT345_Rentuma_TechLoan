package com.example.techloan.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val studentId: String,
    val password: String,
    val confirmPassword: String,
    val role: String  // "STUDENT" or "FACULTY"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean = false,
    val token: String? = null,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    val message: String? = null,
    val user: UserDto? = null,
    val timestamp: String? = null
)

data class UserDto(
    val id: Long? = null,
    val fullName: String? = null,
    val email: String? = null,
    val studentId: String? = null,
    val role: String? = null,
    val penaltyPoints: Int? = null,
    val createdAt: String? = null
)