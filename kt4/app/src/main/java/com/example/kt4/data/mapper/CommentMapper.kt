package com.example.kt4.data.mapper


import com.example.kt4.data.model.CommentDto
import com.example.kt4.domain.Comment

object CommentMapper {
    fun mapToDomain(dto: CommentDto): Comment {
        return Comment(
            postId = dto.postId,
            id = dto.id,
            name = dto.name,
            body = dto.body
        )
    }

    fun mapToDomainList(dtos: List<CommentDto>): List<Comment> {
        return dtos.map { mapToDomain(it) }
    }
}