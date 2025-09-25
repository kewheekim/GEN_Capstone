package com.example.rally.presentation

import GameHealthPayload
import com.example.rally.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val payload = intent.getParcelableExtra<GameHealthPayload>("health_payload")
        setContent { ResultPager( payload ) }
    }

    @Composable
    private fun ResultPager( payload: GameHealthPayload?
    ) {
        val pagerState = rememberPagerState(pageCount = { 4 })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black_bg))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ScoreResultScreen(
                    )

                    1 -> GameTimeScreen(
                    )

                    2 -> HealthDataScreen(
                        data = payload
                    )

                    3 -> FinishScreen(

                    )
                }
            }

            // 하단 페이지 인디케이터
            val indicatorImage = when (pagerState.currentPage) {
                0 -> R.drawable.ic_dot3
                1 -> R.drawable.ic_dot4
                2 -> R.drawable.ic_dot5
                else -> R.drawable.ic_dot6
            }
            Image(
                painter = painterResource(id = indicatorImage),
                contentDescription = "Page Indicator",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(41.dp)
                    .offset(y = -16.dp)
            )
        }
    }
}