package com.example.kt4.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val avatarUrl: String
)