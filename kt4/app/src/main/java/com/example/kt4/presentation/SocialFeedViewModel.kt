package com.example.kt4.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kt4.data.repository.SocialRepository
import com.example.kt4.domain.Comment
import com.example.kt4.domain.Post
import com.example.kt4.domain.PostWithDetails
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SocialFeedViewModel(
    private val repository: SocialRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostWithDetails>>(emptyList())
    val posts: StateFlow<List<PostWithDetails>> = _posts

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var loadJob: Job? = null

    init {
        loadFeed()
    }

    fun loadFeed() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null

            try {
                val posts = repository.getPosts()

                _posts.value = posts.map { post ->
                    PostWithDetails(
                        post = post,
                        isLoadingAvatar = true,
                        isLoadingComments = true
                    )
                }

                coroutineScope {
                    posts.forEachIndexed { index, post ->
                        launch {
                            loadPostDetails(index, post)
                        }
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки ленты: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadPostDetails(index: Int, post: Post) = coroutineScope {
        val avatarDeferred = async {
            try {
                repository.loadAvatarForUser(post.userId)
            } catch (e: Exception) {
                null
            }
        }

        val commentsDeferred = async {
            runCatching { repository.loadCommentsForPost(post.id) }
        }

        val avatar = avatarDeferred.await()
        val comments = commentsDeferred.await()

        _posts.update { currentPosts ->
            currentPosts.toMutableList().apply {
                if (index < size) {
                    set(index, get(index).copy(
                        avatarColor = avatar ?: get(index).avatarColor,
                        comments = comments.getOrElse { emptyList() },
                        isLoadingAvatar = false,
                        isLoadingComments = false,
                        avatarError = avatar == null,
                        commentsError = comments.isFailure
                    ))
                }
            }
        }
    }

    fun refreshFeed() {
        loadFeed()
    }
}