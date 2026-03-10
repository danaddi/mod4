package com.example.kt1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kt1.ui.GithubSearchScreen
import com.example.kt1.presentation.githubSearchViewModelFactory
import com.example.kt1.ui.theme.Kt1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kt1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GithubSearchScreen(
                        context = this@MainActivity,
                        viewModel = viewModel(
                            factory = githubSearchViewModelFactory(this)
                        )
                    )
                }
            }
        }
    }
}
