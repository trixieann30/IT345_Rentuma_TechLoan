package com.example.techloan.shared.network

import com.example.techloan.shared.model.AuthResponse
import com.example.techloan.shared.model.BorrowRequestDto
import com.example.techloan.shared.model.CreateBorrowRequestDto
import com.example.techloan.shared.model.ForgotPasswordRequest
import com.example.techloan.shared.model.GoogleAuthRequestDto
import com.example.techloan.shared.model.InventoryItemDto
import com.example.techloan.shared.model.LoanDto
import com.example.techloan.shared.model.LoginRequest
import com.example.techloan.shared.model.NotificationDto
import com.example.techloan.shared.model.PenaltySummaryDto
import com.example.techloan.shared.model.RegisterRequest
import com.example.techloan.shared.model.ResetPasswordRequest
import com.example.techloan.shared.model.UnreadCountDto
import com.example.techloan.shared.model.UserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface TechLoanApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleAuthRequestDto): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserDto>

    @GET("inventory/available")
    suspend fun getAvailableInventory(
        @Header("Authorization") token: String
    ): Response<List<InventoryItemDto>>

    @POST("reservations")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body dto: CreateBorrowRequestDto
    ): Response<BorrowRequestDto>

    @GET("reservations")
    suspend fun getMyReservations(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long? = null,
        @Query("status") status: String? = null
    ): Response<List<BorrowRequestDto>>

    @GET("loans")
    suspend fun getMyLoans(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long? = null
    ): Response<List<LoanDto>>

    @PUT("reservations/{id}/approve")
    suspend fun approveReservation(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<BorrowRequestDto>

    @PUT("reservations/{id}/reject")
    suspend fun rejectReservation(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<BorrowRequestDto>

    @POST("loans/{id}/return")
    suspend fun returnLoan(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<LoanDto>

    @GET("reservations/{id}")
    suspend fun getReservationById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<BorrowRequestDto>

    @Streaming
    @GET("reservations/{id}/qr")
    suspend fun getReservationQr(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ResponseBody>

    @GET("users/{id}/penalties")
    suspend fun getUserPenalties(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<PenaltySummaryDto>

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<List<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): Response<UnreadCountDto>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): Response<Void>

    @GET("inventory/{id}/auto-image")
    suspend fun getAutoImage(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, String>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, Any>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Map<String, Any>>
}
