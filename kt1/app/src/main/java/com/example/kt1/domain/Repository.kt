package com.example.kt1.domain

import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val id: Long,
    val full_name: String,
    val description: String?,
    val stargazers_count: Int,
    val language: String?
)