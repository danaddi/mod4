package com.example.kt4.domain

data class PostWithDetails(
    val post: Post,
    val avatarColor: String = "#808080",
    val comments: List<Comment> = emptyList(),
    val isLoadingAvatar: Boolean = true,
    val isLoadingComments: Boolean = true,
    val avatarError: Boolean = false,
    val commentsError: Boolean = false
)