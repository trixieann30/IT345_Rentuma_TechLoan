package com.example.techloan.model


data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val message: String?,
    val user: UserDto?
)

data class UserDto(
    val id: Long?,
    val name: String?,
    val email: String?,
    val role: String?
)