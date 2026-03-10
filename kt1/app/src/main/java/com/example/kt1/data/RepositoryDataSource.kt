package com.example.kt1.data

import android.content.Context
import android.util.Log
import com.example.kt1.domain.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

class RepositoryDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private var allRepos: List<Repository>? = null

    suspend fun loadAllRepositories(): List<Repository> = withContext(Dispatchers.IO) {
        if (allRepos != null) return@withContext allRepos!!

        try {
            Log.d("1", "Попытка загрузки файла из assets...")
            val jsonString = context.assets.open("github_repos.json")
                .bufferedReader()
                .use { it.readText() }
            val assets = context.assets
            val fileList = assets.list("")?.joinToString()
            Log.d("1", "Файлы в assets: $fileList")
            allRepos = json.decodeFromString<List<Repository>>(jsonString)
            Log.d("1", "Успешно загружено ${allRepos!!.size} репозиториев")
            allRepos!!
        } catch (e: IOException) {
            Log.e("1", "Ошибка чтения файла: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchRepositories(query: String): List<Repository> = withContext(Dispatchers.IO) {
        delay((500..800).random().toLong())

        val repositories = loadAllRepositories()

        if (query.isBlank()) {
            return@withContext emptyList()
        }

        repositories.filter { repo ->
            repo.full_name.contains(query, ignoreCase = true) ||
                    (repo.description?.contains(query, ignoreCase = true) == true)
        }.sortedByDescending { it.stargazers_count }
    }
}