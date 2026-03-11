package com.example.kt4.domain

data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)