package com.example.kt4.data.datasource


import android.content.Context
import com.example.kt4.data.model.CommentDto
import com.example.kt4.data.model.PostDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class SocialDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadPosts(): List<PostDto> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("social_posts.json")
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString<List<PostDto>>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadComments(): List<CommentDto> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("comments.json")
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString<List<CommentDto>>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}