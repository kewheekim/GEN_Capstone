package com.example.rally.presentation

import com.example.rally.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rally.viewmodel.ScoreViewModel

class ScoreSwipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwipeScreen()
        }
    }
}

@Composable
fun SwipeScreen() {
    val pagerState = rememberPagerState(pageCount = { 2 })  // 페이지 상태
    val viewModel: ScoreViewModel = viewModel()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black))
    ) { page ->
        when (page) {
            0 -> {
                // Match Score Screen
                ScoreScreen(
                    setNumber = 1,
                    opponentName = "상대",
                    userName = "나",
                    viewModel = viewModel
                )
            }

            1 -> {
                // Pause Screen
                PauseScreen(onPause = {})
            }
        }
    }
}