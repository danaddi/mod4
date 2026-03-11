package com.example.kt4.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)