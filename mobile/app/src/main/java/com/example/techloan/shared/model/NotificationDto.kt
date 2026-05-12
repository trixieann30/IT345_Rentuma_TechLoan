package com.example.techloan.shared.model

data class NotificationDto(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String?
)

data class UnreadCountDto(
    val count: Long
)
