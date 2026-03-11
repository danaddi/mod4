package com.example.kt4.data.repository

import com.example.kt4.data.datasource.SocialDataSource
import com.example.kt4.data.mapper.CommentMapper
import com.example.kt4.data.mapper.PostMapper
import com.example.kt4.domain.Comment
import com.example.kt4.domain.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SocialRepository(
    private val dataSource: SocialDataSource
) {

    private var cachedPosts: List<Post>? = null
    private var cachedComments: List<Comment>? = null

    suspend fun getPosts(): List<Post> = withContext(Dispatchers.IO) {
        if (cachedPosts == null) {
            val postDtos = dataSource.loadPosts()
            cachedPosts = PostMapper.mapToDomainList(postDtos)
        }
        cachedPosts!!
    }

    suspend fun getComments(): List<Comment> = withContext(Dispatchers.IO) {
        if (cachedComments == null) {
            val commentDtos = dataSource.loadComments()
            cachedComments = CommentMapper.mapToDomainList(commentDtos)
        }
        cachedComments!!
    }

    suspend fun loadAvatarForUser(userId: Int): String = withContext(Dispatchers.IO) {
        delay((500..1500).random().toLong())

        if (Random.Default.nextInt(100) < 15) {
            throw RuntimeException("Ошибка загрузки аватарки")
        }

        listOf(
            "#FF6B6B",
            "#4ECDC4",
            "#45B7D1",
            "#96CEB4",
            "#FFE194",
            "#B19CD9",
            "#FF9F1C",
            "#2EC4B6"
        )[userId % 8]
    }

    suspend fun loadCommentsForPost(postId: Int): List<Comment> = withContext(Dispatchers.IO) {
        delay((800..2000).random().toLong())

        if (Random.Default.nextInt(100) < 15) {
            throw RuntimeException("Ошибка загрузки комментариев")
        }

        val allComments = getComments()
        allComments.filter { it.postId == postId }
    }
}