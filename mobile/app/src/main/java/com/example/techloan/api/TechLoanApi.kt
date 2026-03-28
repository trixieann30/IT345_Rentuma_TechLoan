package com.example.techloan.api

import com.example.techloan.model.AuthResponse
import com.example.techloan.model.LoginRequest
import com.example.techloan.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TechLoanApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<AuthResponse>
}