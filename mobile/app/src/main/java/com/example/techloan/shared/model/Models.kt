package com.example.techloan.shared.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val studentId: String,
    val password: String,
    val confirmPassword: String,
    val role: String
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

data class InventoryItemDto(
    val id: Long = 0,
    val itemName: String = "",
    val description: String? = null,
    val category: String? = null,
    val availableQuantity: Int = 0,
    val totalQuantity: Int = 0
)

data class CreateBorrowRequestDto(
    val inventoryId: Long,
    val quantity: Int,
    val purpose: String,
    val returnDate: String
)

data class BorrowRequestDto(
    val id: Long = 0,
    val itemName: String? = null,
    val itemDescription: String? = null,
    val quantity: Int = 1,
    val status: String? = null,
    val returnDate: String? = null,
    val dueDate: String? = null,
    val purpose: String? = null,
    val userEmail: String? = null,
    val createdAt: String? = null,
    val rejectReason: String? = null
)

data class LoanDto(
    val id: Long = 0,
    val itemName: String? = null,
    val quantity: Int = 1,
    val borrowedDate: String? = null,
    val dueDate: String? = null,
    val status: String? = null,
    val userEmail: String? = null
)

data class PenaltyDto(
    val id: Long = 0,
    val itemName: String? = null,
    val daysOverdue: Int = 0,
    val penaltyPoints: Int = 0,
    val paid: Boolean = false
)

data class PenaltySummaryDto(
    val totalPoints: Int = 0,
    val penalties: List<PenaltyDto> = emptyList()
)

data class GoogleAuthRequestDto(
    val idToken: String,
    val role: String,
    val personalEmail: String? = null
)
