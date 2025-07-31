package com.example.rally.presentation

import com.example.rally.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    val pagerState = rememberPagerState(pageCount = { 2 })
    val viewModel: ScoreViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg))
    ) {
        Column (modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ScoreScreen(
                        setNumber = 1,
                        opponentName = "상대",
                        userName = "나",
                        viewModel = viewModel
                    )
                    1 -> PauseScreen(onPause = {})
                }
            }

            // 현재 페이지에 따라 이미지 선택
            val indicatorImage = when (pagerState.currentPage) {
                0 -> R.drawable.ic_dot1
                else -> R.drawable.ic_dot2
            }

            Image(
                painter = painterResource(id = indicatorImage),
                contentDescription = "Page Indicator",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(18.dp)
            )
        }
    }
}