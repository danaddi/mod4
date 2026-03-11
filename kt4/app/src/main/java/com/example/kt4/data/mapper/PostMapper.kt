package com.example.kt4.data.mapper

import com.example.kt4.data.model.PostDto
import com.example.kt4.domain.Post

object PostMapper {
    fun mapToDomain(dto: PostDto): Post {
        return Post(
            id = dto.id,
            userId = dto.userId,
            title = dto.title,
            body = dto.body,
            avatarUrl = dto.avatarUrl
        )
    }

    fun mapToDomainList(dtos: List<PostDto>): List<Post> {
        return dtos.map { mapToDomain(it) }
    }
}